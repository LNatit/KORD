# AGENTS.md - Chord Mod Development Guide

This guide helps AI agents understand the Chord mod architecture and development patterns for productive contributions.

## Project Overview

**Chord** is a NeoForge 1.21.1+ Minecraft mod that provides sophisticated keyboard keybinding conflict detection and resolution. It analyzes key conflicts using seven orthogonal semantic dimensions rather than simple button matching.

**Key Technologies:**
- NeoForge 21.1.222 (Minecraft 1.21.1)
- Java 21
- Gradle build system with ModDevGradle plugin
- Mixin for bytecode manipulation
- Custom data-driven JSON configuration via resource reloaders

## Architecture: The Seven Semantic Dimensions

Chord's core innovation: all key conflict analysis flows through **7 evaluation dimensions** that are completely independent:

1. **Key Context** - Where the keybind applies (IN_GAME, IN_GUI, UNIVERSAL, etc.) via NeoForge's `IKeyConflictContext`
2. **State Set** - Runtime conditions enabling the keybind (mutually exclusive states create automatic safety)
3. **Input Interception** - Whether the binding consumes input, preventing propagation to other bindings
4. **Redirect Mode** - Whether binding switches context (e.g., opening inventory)
5. **Resource** - What game object the binding affects, with concurrency properties
6. **Intent** - User intent category (semantic similarity between seemingly different bindings)
7. **Modality** - Physical operation mode: Press (P), Hold (H), Toggle (T), etc.

## Pipeline Architecture

The conflict evaluation follows **8 sequential stages** (see `doc/PIPELINE_zh-cn.md` for full detail):

**Execution Flow:**
```
Stage 1: Physical Key Match 
  → Stage 2: User Overrides [TODO]
  → Stage 3: Context Overlap & Semantic Routing
  → Stage 4: State Mutex Analysis (evaluateStateMutex)
  → Stage 5: Input Interception (evaluateIntercept)
  → Stage 6: Redirect Mode (evaluateRedirect)
  → Stage 7: Resource Conflict (evaluateResource) ← moved forward from Stage 8
  → Stage 8: Intent & Modality (evaluateIntent + evaluateModality)
```

Key design: Each stage can mark results as **finished** (preventing further evaluation), or produce **dynamic risks** that propagate to later stages and can be downgraded/escalated based on new information.

**Core class:** `com.lnatit.chord.eval.Evaluator.java`
- **`Evaluator` is an `interface`** (not a class) — all methods are `static`
- Static method: `Evaluator.conflicts(KeyMapping, KeyMapping): ConflictResult`
- Method `eval(ContextSemantic, ContextSemantic): ContextCollector` runs the 6-stage evaluation pipeline for a single semantic context pair
- `ContextCollector` accumulates risks, tags, and state through the pipeline for a single context

## Critical Semantic Concepts

### KeySemantic Record
Semantic payload is split into a key-level wrapper plus per-context record. **KeySemantic is a sealed interface** with two implementations:
```java
public sealed interface KeySemantic permits Semantical, RawContext {
    // Semantical: Multi-context with detailed per-context semantics (from datapack definitions)
    record Semantical(LinkedHashMap<KeyContext, ContextSemantic> semanticMap) implements KeySemantic {}
    
    // RawContext: Single context with no detailed semantic definition (fallback)
    record RawContext(KeyContext context) implements KeySemantic {}
}

record ContextSemantic(StateSet states, boolean intercept, RedirectMode redirectMode,
                       Resource resource, boolean readOnly, IntentList intents, Modality modality)
```

**Conflict Detection Routing:**
- If **both** KeyMappings have `Semantical` semantics → use full 6-stage pipeline (`Evaluator.eval()`) for each overlapping context
- If **at least one** has `RawContext` → downgrade to simple context-overlap check (no detailed semantic analysis)

### State Mutex vs Subset  
- **Mutex**: Two StateSet instances share zero common states → returns `true` from `evaluateStateMutex()`, terminates pipeline with SAFE severity
- **Intersect**: Two StateSets have partial overlap → creates `STATE_INTERSECT_RISK` (diagnostic severity SAFE), continues evaluation
- **Subset**: StateSet A ⊂ StateSet B → creates `StateTag.StateSubset(leftIsSubset: boolean)` record, continues to next stage
  - The `leftIsSubset` flag indicates which binding has the narrower state range
  - This info is crucial in `evaluateIntercept()` to determine if partial override can occur
- **StateSet tree structure**: `StateSet` is backed by a boolean expression tree (`eval/mutex/tree/`) composed of `LeafNode` (a named mutex set), `AndNode`, `OrNode`, and `NotNode`. The codec in `Codecs.STATES_CODEC` parses these automatically from JSON.

### Interceptive Bindings
Input interception evaluation in `evaluateIntercept()`:
- **No interception**: Both bindings receive input concurrently → `CONCURRENT_INPUT` (diagnostic)
- **One-way interception**: One binding intercepts → `INTERCEPT_INPUT` (WARNING severity)
- **Mutual interception**: Both intercept → `RACE_CONDITION` (SEVERE severity)
- **Partial Override**: Interception exists AND state subset relationship means one binding always/never intercepts within the other's active states → `PARTIAL_OVERRIDE`, terminates evaluation with INFO/WARNING severity

These are `RiskEntry<InterceptTag>` records, not mutable `DynamicRisk` instances.

### Resource Conflict Matrix
Detected in `evaluateResource()`: overlapping resources trigger CONCURRENT_WRITE (severe) unless both read-only or protected by interception.

## Data Flow & Extensibility

### JSON Configuration
Keys and their semantics loaded from `key_semantics/**/*.json` at client reload:
- Entry point: `KeySemanticManager` (extends `SimpleJsonResourceReloadListener`)
- Parsed via: `com.lnatit.chord.data.Codecs.KEYS_CODEC`
- Applied to KeyMapping via Mixin-injected `SemanticalKey.chord$setSemantic()`

**Four reload listeners** are registered in `Chord.java`:
1. `MutexSetManager.INSTANCE` — loads mutex group definitions from `mutex_sets/**/*.json` (via `data/mutex/`)
2. `ResourceReloadListener.INSTANCE` — loads resource concurrency definitions from `resources/**/*.json`
3. `KeySemanticManager.INSTANCE` — loads key semantic definitions from `key_semantics/**/*.json`
4. `DatapackOverrideReloader.INSTANCE` — loads conflict overrides from `builtin_overrides/**/*.json`

### Mixin Integration
- **Client mixin:** `client.MixinKeyMapping` - Extends KeyMapping with semantic storage
- Config: `src/main/resources/chord.mixins.json`
- Accesses KeyMapping semantics via cast: `((SemanticalKey) keyMapping).chord$getSemantic()`

### Result Types
`com.lnatit.chord.result/` package defines conflict output:
- `ConflictResult` - Final serializable result (also has `ConflictResult.SAFE` constant)
- `ConflictRisk` - Interface; `ConflictRisk.Static` is the serializable implementation; `DynamicRisk` is the mutable subclass
- `ConflictTag` - Static debug tags (HARDWARE_MISMATCH, STATE_MUTEX, etc.)
- `DynamicRisk` - Mutable risks with severity escalation/downgrade logic; concrete subclasses: `StateSubset`, `InterceptInput`, `RaceCondition`, `ContextLeak`, `DeferredRisk`, `LoseFocus`, `InputBlock`
- `Severity` enum - SAFE → INFO → WARNING → SEVERE

## Build & Run Commands

**Important:** This uses ModDevGradle which requires specific workflow:

```bash
# Standard development setup
./gradlew --refresh-dependencies    # Recover from dependency issues
./gradlew clean                     # Full reset (doesn't affect source code)
./gradlew build                     # CI-equivalent full build

# Run configurations (defined in build.gradle)
./gradlew runClient                 # Launch client with mod loaded
./gradlew runServer                 # Launch dedicated server with mod
./gradlew runData                   # Run data generation (generates JSON schemas, block/item definitions)
./gradlew runGameTestServer         # Execute registered game tests

# Data generation
./gradlew runData --args='--mod chord --all --output src/generated/resources/ --existing src/main/resources/'
```

**Generated resources:** Data generators output to `src/generated/resources/` (excluded from Git, regenerated on build).

## Project Structure

```
src/main/java/com/lnatit/chord/
├── Chord.java                      # Mod entry point, registers 4 reload listeners
├── eval/
│   ├── Evaluator.java             # Core pipeline as interface with static methods (CRITICAL)
│   ├── Modality.java              # P/H/T mode evaluation matrix
│   ├── RedirectMode.java          # Context transition evaluation matrix
│   ├── context/                   # IKeyContext adapter layer
│   ├── intent/                    # Intent semantics (TODO: incomplete)
│   ├── mutex/
│   │   ├── StateSet.java          # Boolean expression over mutex groups
│   │   └── tree/                  # AndNode, OrNode, NotNode, LeafNode, TreeNode
│   ├── override/
│   │   ├── OverrideManager.java   # Static map of overrides keyed by OverrideType priority
│   │   └── OverrideType.java      # Override source types: USER > PLAYER > CREATOR > BUILTIN
│   └── resource/                  # Resource concurrency properties
├── semantic/
│   ├── ContextSemantic.java       # Record of 7 semantic dimensions; ContextSemantic.DEFAULT available
│   ├── KeySemantic.java           # Key-level semantic container (AS_IS / Precise)
│   ├── KeyContext.java            # Key context wrapper type for semantic maps
│   └── SemanticalKey.java         # Interface injected onto KeyMapping via Mixin
├── data/
│   ├── Codecs.java                # JsonCodec definitions (KEYS_CODEC, OVERRIDE_DEFINITION_CODEC, MUTEX_DEFINITIONS_CODEC, etc.)
│   ├── Requirement.java           # Mod-id + version-range requirement check for conditional entries
│   ├── mutex/
│   │   ├── MutexSetManager.java   # Reload listener for mutex_sets/**/*.json
│   │   ├── MutexSet.java          # A named group of mutually exclusive states
│   │   └── MutexDefinition.java   # Deserialized JSON structure for mutex groups
│   ├── semantic/
│   │   ├── KeySemanticManager.java  # Reload listener for key_semantics/**/*.json
│   │   └── KeyDefinitions.java      # Deserialized JSON structure
│   ├── resource/
│   │   ├── ResourceReloadListener.java  # Reload listener for resources/**/*.json
│   │   └── ResourceDefinition.java      # Deserialized JSON resource entry
│   └── override/
│       ├── DatapackOverrideReloader.java  # Reload listener for builtin_overrides/**/*.json
│       └── OverrideDefinition.java        # Deserialized JSON override entry
├── result/                        # Conflict result & risk types
├── mixin/client/                  # Bytecode injection points
└── util/                          # Matrix & collection utilities (AsymmetricEnumMatrix, Provider, Supplier)
```

## Common Development Patterns

### Adding a New Evaluation Dimension
1. Extend `ContextSemantic` record with new field
2. Create evaluation method in `Evaluator` following the pattern of `evaluateStateMutex()`:
   - Query both semantics
   - Populate collector with debug tags or dynamic risks
   - Set finished() if this dimension alone determines outcome
3. Update `Codecs.SEMANTIC_CODEC` so JSON datapacks can decode the new field
4. Call new evaluator from `eval()` pipeline in correct ordering

### Matrix-Based Decision Logic
Several subsystems use 2D enum matrices (RedirectMode combinations, Modality combinations):
- Pattern: `AsymmetricEnumMatrix<Enum, ResultType>`
- Lookup: `RedirectMode.MATRIX.get(mode1, mode2)` returns `ConflictInfo`
- See `Modality.java` for fully documented matrix with recovery cost rationale

### Adding Dynamic Risks
Create inner class in `DynamicRisk.java`:
```java
public static class YourRisk extends DynamicRisk {
    public void escalate() { ... }  // severity adjustment
    public void downgrade() { ... }
}
```
Use `collector.getRisk(YourRisk.class)` to check for and modify previous risks.

### Adding Override Entries (datapack)
Create a JSON file under `src/main/resources/data/chord/builtin_overrides/` decoded via `Codecs.OVERRIDE_DEFINITION_CODEC`:
```json
{ "is_builtin": true, "key1": { "name": "key.mod.action1" }, "key2": { "name": "key.mod.action2" }, "result": { "severity": "SAFE", "risks": [] } }
```
- `is_builtin: true` → stored as `OverrideType.BUILTIN`; `false` → `OverrideType.CREATOR`
- Pair lookup is order-independent (symmetric `equals`/`hashCode` in `OverrideManager.Pair`
- Priority order: USER > PLAYER > CREATOR > BUILTIN (see `OverrideManager.PRIORITY`)

### Adding Mutex State Groups (datapack)
Create a JSON file under `src/main/resources/data/chord/mutex_sets/` decoded via `Codecs.MUTEX_DEFINITIONS_CODEC`:
```json
{ "namespace": "mymod", "requirements": [{ "modid": "mymod" }], "mutexes": ["state_a", "state_b", "state_c"] }
```

### Adding Resource Definitions (datapack)
Create a JSON file under `src/main/resources/data/chord/resources/` decoded via `Codecs.RESOURCE_DEFINITION_CODEC`:
```json
{ "path": "player/inventory", "supports_concurrent_writes": false }
```
- If `path` is omitted, `ResourceReloadListener` falls back to the JSON resource id path.

## Documentation Resources

- **Architecture:** `doc/CONTRIBUTOR_GUIDE_zh-cn.md` - Explains 7 dimensions and 3 analysis layers
- **Pipeline:** `doc/PIPELINE_zh-cn.md` - Detailed stage-by-stage evaluation flow with big-picture metaphors
- **Neoforge Docs:** https://docs.neoforged.net/
- **Parchment Mappings:** Enhanced parameter names via `parchment_mappings_version` in `gradle.properties`

*Note: All documentation(include this file) may be outdated as the project evolves - always refer to code for the most accurate information.*

## IDE Configuration

- **Language Level:** Java 21 (forced by NeoForge)
- **IDEA Settings:** build.gradle configures automatic source/javadoc downloads
- **Run Configs:** ModDevGradle auto-generates client/server/data runs in IDE
- **Rebuild Trigger:** Save any groovy or properties file in workspace to trigger `generateModMetadata` sync

## Key Files for Review

When implementing new features:
1. **Entry:** `Chord.java` - Mod initialization and reload listener registration
2. **Core Logic:** `Evaluator.java` - All conflict detection rules
3. **Data Model:** `ContextSemantic.java` + `KeySemantic.java` - Semantic dimensions and key-level wrapper
4. **Configuration:** `neoforge.mods.toml` template - Mod metadata (auto-expanded at build)
5. **Mixins:** `chord.mixins.json` - Bytecode injection declarations

## Common Pitfalls

- **Stage Order Matters:** Never reorder evaluateXXX() calls - later stages depend on earlier risk state
- **Immutability:** `ContextSemantic` is immutable; all runtime conflict adjustments go through `ConflictCollector`
- **Context Overlap:** Use `isContextOverlapping()` before evaluating semantics - prevents false positives
- **Resource Root Sentinel:** Use `Resource.ROOT` to represent no specific target; avoid nullable resource semantics
- **State Subset Escalation:** Only escalates StateSubset risk if interception is present - track this coupling
- **Evaluator is an interface:** Do not attempt to instantiate it; call static methods directly (`Evaluator.conflicts(...)`, `Evaluator.eval(...)`)
- **Override cleared on reload:** `DatapackOverrideReloader` clears both `BUILTIN` and `CREATOR` types on each reload — user-level overrides (`USER`, `PLAYER`) must be repopulated separately

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

```
Stage 1: Physical Key Match → Stage 2: User Overrides → Stage 3: Context Overlap
  → Stage 4-8: Fine-grained Semantic Analysis
```

Key design: Each stage can mark results as **finished** (preventing further evaluation), or produce **dynamic risks** that propagate to later stages and can be downgraded/escalated based on new information.

**Core class:** `com.lnatit.chord.eval.Evaluator.java`
- Static method: `Evaluator.conflicts(KeyMapping, KeyMapping): ConflictResult`
- Each `evaluateXXX()` method handles one analysis dimension
- `ConflictCollector` accumulates risks, tags, and completion state through the pipeline

## Critical Semantic Concepts

### KeySemantic Record
Defined in `eval/KeySemantic.java` - the immutable data container:
```java
record KeySemantic(StateSet states, boolean intercept, RedirectMode redirectMode,
                   Resource resource, boolean readOnly, List<Intent> intents, Modality modality)
```

### State Mutex vs Subset
- **Mutex**: Two StateSet instances share zero common states → automatic SAFE resolution
- **Subset**: StateSet A ⊂ StateSet B → creates DynamicRisk.StateSubset (gets upgraded to WARNING/SEVERE if interceptive)

### Interceptive Bindings
If either binding intercepts input, it may create:
- `DynamicRisk.InterceptInput` - One-way interception
- `DynamicRisk.RaceCondition` - Both intercept (undefined behavior)
- These escalate state_subset risks and can downgrade other risks if intents overlap

### Resource Conflict Matrix
Detected in `evaluateResource()`: overlapping resources trigger CONCURRENT_WRITE (severe) unless both read-only or protected by interception.

## Data Flow & Extensibility

### JSON Configuration
Keys and their semantics loaded from `key_semantics/**/*.json` at client reload:
- Entry point: `KeySemanticManager` (extends `SimpleJsonResourceReloadListener`)
- Parsed via: `com.lnatit.chord.data.Codecs.KEYS_CODEC`
- Applied to KeyMapping via Mixin-injected `SemanticalKey.chord$addSemantic()`

### Mixin Integration
- **Client mixin:** `client.MixinKeyMapping` - Extends KeyMapping with semantic storage
- Config: `src/main/resources/chord.mixins.json`
- Accesses KeyMapping semantics via cast: `((SemanticalKey) keyMapping).chord$getSemanticEntries()`

### Result Types
`com.lnatit.chord.result/` package defines conflict output:
- `ConflictResult` - Final serializable result
- `ConflictTag` - Static debug tags (HARDWARE_MISMATCH, STATE_MUTEX, etc.)
- `DynamicRisk` - Mutable risks with severity escalation/downgrade logic
- `Severity` enum - SAFE → INFO → WARNING → SEVERE

## Build & Run Commands

**Important:** This uses ModDevGradle which requires specific workflow:

```bash
# Standard development setup
./gradlew --refresh-dependencies    # Recover from dependency issues
./gradlew clean                     # Full reset (doesn't affect source code)

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
├── Chord.java                      # Mod entry point, registers reload listeners
├── eval/
│   ├── Evaluator.java             # Core 8-stage conflict pipeline (CRITICAL)
│   ├── KeySemantic.java           # Record of 7 semantic dimensions
│   ├── SemanticalKey.java         # Interface injected onto KeyMapping via Mixin
│   ├── Modality.java              # P/H/T mode evaluation matrix
│   ├── RedirectMode.java          # Context transition evaluation matrix
│   ├── context/                   # IKeyContext adapter layer
│   ├── intent/                    # Intent semantics (TODO: incomplete)
│   ├── mutex/                     # StateSet mutual exclusion logic
│   └── resource/                  # Resource concurrency properties
├── data/
│   ├── Codecs.java                # JsonCodec definitions for key_semantics JSON
│   ├── semantic/
│   │   ├── KeySemanticManager.java  # Resource reload listener
│   │   └── KeyDefinitions.java      # Deserialized JSON structure
│   └── override/                  # User override persistence (planned)
├── result/                        # Conflict result & risk types
├── mixin/client/                  # Bytecode injection points
└── util/                          # Matrix & collection utilities
```

## Common Development Patterns

### Adding a New Evaluation Dimension
1. Extend `KeySemantic` record with new field
2. Create evaluation method in `Evaluator` following the pattern of `evaluateStateMutex()`:
   - Query both semantics
   - Populate collector with debug tags or dynamic risks
   - Set finished() if this dimension alone determines outcome
3. Call new evaluator from `eval()` pipeline in correct ordering

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
3. **Data Model:** `KeySemantic.java` - Semantic dimensions representation
4. **Configuration:** `neoforge.mods.toml` template - Mod metadata (auto-expanded at build)
5. **Mixins:** `chord.mixins.json` - Bytecode injection declarations

## Common Pitfalls

- **Stage Order Matters:** Never reorder evaluateXXX() calls - later stages depend on earlier risk state
- **Immutability:** KeySemantic is immutable; all modifications go through ConflictCollector
- **Context Overlap:** Use `isContextOverlapping()` before evaluating semantics - prevents false positives
- **Resource Null:** Resource field can be null (bindings without specific resource targets)
- **State Subset Escalation:** Only escalates StateSubset risk if interception is present - track this coupling


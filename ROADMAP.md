# Chord Mod Development Roadmap

Pragmatic roadmap for the Chord keybinding conflict mod, aligned to current source code status. Last updated: May 2026.

---

## Executive Summary

**Current Status:** The evaluator core is implemented (hardware check, overrides lookup, context routing, six semantic-stage context evaluation). The semantic model has already been refactored to `KeySemantic.Semantical` / `KeySemantic.RawContext` with collector-based risk accumulation (`RiskEntry`, `Finalized`, `ConflictRisk.Packed`).

**Primary Gaps:**
- Datapack decode path is wired, but sample datapacks and schema validation coverage are still missing.
- No persisted user overrides (`USER`/`PLAYER`) yet.
- UI is scaffold-only (`gui/KeyBindingScreen.java`).
- No automated test suite (`src/test/java` absent).
- No bundled `data/chord/**` JSON samples in resources.

**Vision:** Deliver a complete, user-facing mod that can detect, explain, and help resolve keybinding conflicts across vanilla and modded environments.

---

## Verified Baseline (Code-As-Truth)

### Implemented and Working in Code

1. **Evaluator Pipeline** (`src/main/java/com/lnatit/chord/eval/Evaluator.java`)
   - `Evaluator` is an interface with static methods.
   - `conflicts(KeyMapping, KeyMapping)` flow exists.
   - Stage order in context evaluation is implemented as:
     - `evaluateStateMutex`
     - `evaluateIntercept`
     - `evaluateRedirect`
     - `evaluateResource`
     - `evaluateIntent`
     - `evaluateModality`

2. **Semantic Model** (`src/main/java/com/lnatit/chord/semantic/`)
   - `KeySemantic` is sealed with `Semantical` and `RawContext`.
   - `ContextSemantic` contains the 7 dimensions.
   - `KeySemantic.fallback(...)` routes unknown contexts to anonymous `ConflictType.CUSTOM` wrappers.

3. **Result Model** (`src/main/java/com/lnatit/chord/result/`)
   - Uses `ConflictResult`, `Finalized`, `ConflictRisk`, `RiskEntry`, `RiskTag`, `Severity`.
   - Per-stage tags are in `result/context/*Tag`.
   - Mutable risk adjustment uses `RiskEntry.Simple`, not a separate `DynamicRisk` hierarchy.

4. **Reload Listener Registration** (`src/main/java/com/lnatit/chord/Chord.java`)
   - 5 listeners registered:
     - `ContextReloadListener`
     - `MutexSetManager`
     - `ResourceReloadListener`
     - `KeySemanticManager`
     - `DatapackOverrideReloader`

5. **Mixin Integration**
   - `src/main/resources/chord.mixins.json` includes `client.MixinKeyMapping`.
   - `MixinKeyMapping` stores semantic payload through `SemanticalKey` methods.

### Partially Implemented / Blocked

1. **Datapack Runtime Validation & Fixtures**
   - `ContextReloadListener.decodeDefinition(...)`, `KeySemanticManager.decodeDefinition(...)`, and
     `DatapackOverrideReloader.decodeDefinition(...)` are now wired to `Codecs`.
   - Remaining gaps are fixture coverage and schema regression checks for real datapack inputs.

2. **Intent Dimension**
   - `Intent` has decode-stage cache (`beginDecode`/`endDecode`) and `Intent.of(...)` intern-like behavior during decode.
   - `IntentList.hasShared(...)` and `IntentList.isIdentical(...)` are implemented.
   - Practical impact now depends on real semantic datapack coverage (not decode wiring).

3. **UI Layer**
   - Only scaffold exists: `src/main/java/com/lnatit/chord/gui/KeyBindingScreen.java`.

4. **Tests**
   - No `src/test/java` in repository.

---

## What Was Corrected in This Roadmap

The following outdated references were removed from planning assumptions:

- Old result model terms (`ConflictTag`, `DynamicRisk`, `ConflictCollector`) -> replaced with current `RiskTag`/`RiskEntry`/collector model.
- Old semantic/context model claims (`IKeyContext` enum workflow, `AS_IS`) -> replaced with current `KeyContext` registry + `ConflictType`.
- Old override symbols (`Type`, `withSourceTag`, `Pair`-centric docs) -> replaced with current `OverrideType`, `KeyPair`, `OverrideManager` map priority.
- Incorrect listener count claims (3/4 listeners) -> normalized to current 5-listener registration.
- Claims that context/key semantic/override datapack decode is fully operational -> corrected to placeholder status.

---

## Phase Progress (Reality-Based)

- **Phase 1 - Core Stabilization:** In progress
  - Core pipeline exists.
  - Lacks tests and edge-case verification for state/resource logic.
- **Phase 2 - Intent:** Partially complete
  - Matching helpers exist.
  - Decode pipeline exists; lacking fixture-driven validation and behavior tests.
- **Phase 3 - Override & User Config:** Partially complete
  - In-memory priority map exists.
  - Missing persisted `USER`/`PLAYER` storage.
- **Phase 4 - Runtime Event Integration:** Partially complete
  - Reload hook exists.
  - No periodic/runtime conflict scan hooks.
- **Phase 5 - UI/UX:** Not started (beyond scaffold).
- **Phase 6 - Testing & Docs:** Testing not started; docs partially available.
- **Phase 7 - Performance & Polish:** Not started.

---

## Development Roadmap

### Phase 1: Stabilize Core + Verify Correctness (Priority: Critical)

**Goal:** Make existing evaluator behavior trustworthy and regression-safe.

- [ ] Add test project skeleton under `src/test/java/com/lnatit/chord/`.
- [ ] Add evaluator behavior tests for:
  - state mutex / subset / intersect paths
  - intercept race and partial override
  - redirect + modality coupling
  - resource conflict severities
- [ ] Add algebra property tests for `StateSet` (`intersect`, `union`, `complement`, subset relations).
- [ ] Resolve or validate `// TODO recheck` blocks in `StateSet.HyperRect.intersect()` and `StateSet.HyperRect.complement()`.

**Deliverable:** stable core with baseline automated regression tests.

### Phase 2: Finish Datapack Decode Wiring (Priority: Critical)

**Goal:** Stabilize the now-wired data pipeline with fixtures and validation.

- [x] Implement `ContextReloadListener.decodeDefinition(...)`.
- [x] Implement `KeySemanticManager.decodeDefinition(...)`.
- [x] Implement `DatapackOverrideReloader.decodeDefinition(...)`.
- [x] Reconcile `Codecs` declarations with active loader usage.
- [ ] Add at least one valid sample for each data source:
  - `data/chord/contexts/*.json`
  - `data/chord/key_semantics/*.json`
  - `data/chord/builtin_overrides/*.json`
- [ ] Add failure-case fixtures (bad context id / bad tree node / invalid semantic kind) to lock schema behavior.

**Deliverable:** datapack-based semantic and override data can be loaded end-to-end.

### Phase 3: User Override Persistence (Priority: High)

**Goal:** Let users persist custom conflict resolutions.

- [ ] Add `UserOverrideManager` to load/save `USER` and `PLAYER` overrides.
- [ ] Define config file format under `config/chord/`.
- [ ] Hook lifecycle points for startup load and runtime save.
- [ ] Ensure loaded entries populate `OverrideManager.OVERRIDES` using `OverrideType.USER` / `OverrideType.PLAYER`.

**Deliverable:** user overrides survive restart and participate in override priority.

### Phase 4: Runtime Integration Events (Priority: Medium)

**Goal:** Trigger conflict analysis at meaningful client lifecycle points.

- [ ] Add client event subscriber class (new).
- [ ] Hook keymapping registration/update moments to schedule scans.
- [ ] Add optional periodic scan strategy (throttled tick-based).
- [ ] Define where latest conflict snapshot is stored for UI access.

**Deliverable:** conflict analysis runs automatically, not only on-demand.

### Phase 5: UI/UX Delivery (Priority: Medium)

**Goal:** Provide usable conflict visualization and management.

- [ ] Evolve `gui/KeyBindingScreen.java` from scaffold to functional screen.
- [ ] Add conflict list view with severity/context breakdown.
- [ ] Add detail panel for per-dimension risk explanation.
- [ ] Add override actions (safe/warn/ignore/reset) backed by persistence layer.
- [ ] Add localization resources under `assets/chord/lang/`.

**Deliverable:** end users can inspect and resolve conflicts in game.

### Phase 6: Documentation and Quality Hardening (Priority: Medium)

**Goal:** Make maintenance and onboarding reliable.

- [ ] Keep `AGENTS.md` + `ROADMAP.md` synced with code symbols.
- [ ] Add JSON format docs for contexts/semantics/overrides.
- [ ] Add contributor-facing testing guide.
- [ ] Add API-level JavaDoc for key public structures.

**Deliverable:** clear contributor workflow and lower future drift risk.

### Phase 7: Performance and Polish (Priority: Low)

**Goal:** Improve runtime efficiency after correctness is established.

- [ ] Measure evaluator throughput for realistic keybinding counts.
- [ ] Add caching only after profiling confirms hotspots.
- [ ] Review logging granularity (info/warn/debug consistency).
- [ ] Run dependency CVE checks before release branch.

**Deliverable:** production-polished behavior with measured performance baseline.

---

## Success Criteria

### Functional
- [ ] Datapack decode path works for contexts, semantics, and overrides.
- [ ] Intent stage has meaningful runtime effect through loaded semantic data.
- [ ] User override persistence works across sessions.
- [ ] UI supports inspect + override loop end-to-end.

### Quality
- [ ] Automated tests cover evaluator stage interactions and state algebra edge cases.
- [ ] No known severe regressions in core conflict evaluation.
- [ ] Docs reference current symbols and file layout.

---

## Immediate Next Actions (Recommended)

1. Add minimal working fixtures for `contexts`, `key_semantics`, and `builtin_overrides` under `src/main/resources/data/chord/`.
2. Add first evaluator regression tests (state/intercept/resource focus).
3. Introduce `UserOverrideManager` contract and persistence format.
4. Start GUI only as a thin prototype after (1) and (2), to avoid building UI on unverified data behavior.

---

## Source-of-Truth Note

Roadmap status is derived from current source under `src/main/java/com/lnatit/chord/` and `src/main/resources/`. If code and roadmap disagree, code is authoritative and roadmap should be updated immediately.

# Chord Mod Development Roadmap

Strategic roadmap for the Chord keybinding conflict detection mod, organized by phase and priority. Last updated: April 2026.

---

## Executive Summary

**Current Status:** Core conflict analysis engine (~80% complete) with functional evaluation pipeline, semantic data model, Mixin integration, fully operational override loading pipeline (BUILTIN/CREATOR), and mutex set data loading. Primary gaps: Intent system, User override persistence (USER/PLAYER level), UI/UX, and comprehensive testing.

**Vision:** Transform Chord from a backend analysis engine into a complete user-facing mod that intelligently detects, communicates, and helps resolve keyboard keybinding conflicts across Minecraft and modded environments.

---

## Architecture Assessment

### ✅ Completed Components

#### 1. **Core Evaluation Pipeline** (Evaluator.java)
- **Status:** 90% Complete
- **Implementation:**
  - 8-stage conflict detection pipeline fully implemented
  - 7 semantic dimensions captured in KeySemantic record
  - Dynamic risk propagation and escalation logic working
  - ConflictCollector pattern for multi-stage accumulation
- **Quality:** Well-structured, immutable design, stage ordering locked
- **What's Ready:**
  - Hardware matching (`isSameKey`)
  - Context overlap detection (`isContextOverlapping`)
  - State mutex evaluation (`evaluateStateMutex`)
  - Input interception analysis (`evaluateIntercept`)
  - Redirect mode matrix routing (`evaluateRedirect`)
  - Resource conflict detection (`evaluateResource`)
  - Modality conflict matrix (`evaluateModality`)

#### 2. **Semantic Dimension Models**
- **KeySemantic Record:** ✅ Complete - immutable 7-field record with `KeySemantic.DEFAULT` constant
- **State Set Logic** (StateSet.java): ✅ 85% - Sealed interface with HyperRect/UnionSet, full boolean algebra (intersect/union/complement/subset) implemented; two `// TODO recheck` markers remain in `HyperRect.intersect()` and `HyperRect.complement()` but logic is functional
- **Modality Matrix** (Modality.java): ✅ 95% - Complete enum with 2D conflict matrix (P/H/T/C combinations), recovery cost rationale documented
- **RedirectMode Matrix** (RedirectMode.java): ✅ 95% - Complete enum with asymmetric matrix covering context transitions
- **Resource Model** (Resource.java): ✅ 80% - Node/ROOT structure, `overlaps()` uses LCA path matching; marked `// TODO`, needs edge-case hardening and null-safety review

#### 3. **Mixin Integration**
- **MixinKeyMapping:** ✅ Complete - Injects semantic storage into KeyMapping via SemanticalKey interface
- **chord.mixins.json:** ✅ Configured - Client-side mixin with default require = 1
- **Integration Points:** 
  - KeyMapping extended with `chord$semantics` HashMap
  - Clear/add/get semantic operations working

#### 4. **Data Loading & Serialization**
- **MutexSetManager:** ✅ Complete - Fully operational `SimpleJsonResourceReloadListener` for `mutex_sets/**/*.json`; handles namespace inference, requirement gating, and override logging
- **KeySemanticManager:** ✅ 90% - SimpleJsonResourceReloadListener implementation functional
- **DatapackOverrideReloader:** ✅ Complete - Fully operational; loads `builtin_overrides/**/*.json`, populates `OverrideManager` with `BUILTIN`/`CREATOR` entries, clears both on reload
- **Codecs.java:** ✅ 90% - Complete Codec definitions for all 7 dimensions + state tree codecs (And/Or/Not/Leaf nodes), `MUTEX_DEFINITIONS_CODEC`, `OVERRIDE_DEFINITION_CODEC`
- **JSON Schema Support:** ✅ Partial - Codecs ready, no sample `key_semantics/` or `builtin_overrides/` JSON files in resources yet

#### 5. **Result & Risk Types**
- **ConflictResult:** ✅ Complete - Immutable severity + risks list; `ConflictResult.SAFE` constant present
- **ConflictTag:** ✅ Complete - All static debug tags defined including all four override-source tags (`USER_OVERRIDE`, `BUILTIN_OVERRIDE`, `CREATOR_OVERRIDE`, `PLAYER_OVERRIDE`)
- **Severity Enum:** ✅ Complete - SAFE/INFO/WARNING/SEVERE with downgrade logic
- **DynamicRisk Hierarchy:** ✅ 95% - All concrete subclasses fully implemented:
  - `StateSubset` (escalation → `PARTIAL_OVERRIDE` tag)
  - `InterceptInput` / `RaceCondition` (abstract `Interceptive` base)
  - `ContextLeak` / `LoseFocus` (abstract `SingleModifier` base with modality-driven severity)
  - `DeferredRisk` (HOLD/TOGGLE/PRESS severity matrix)
  - `InputBlock` (KEY+MOUSE modality matrix)
  - `ModalJudged` abstract base for modal-sensitive risks

#### 6. **Override Manager** (eval/override/)
- **Status:** 75% Complete
- **Implemented:**
  - `OverrideManager` interface with `EnumMap`-backed storage, `put`/`get`/`clear`/`clearAll` static methods
  - `Pair` record with symmetric `equals`/`hashCode` (order-independent lookup)
  - Priority list: `USER > PLAYER > CREATOR > BUILTIN`
  - `withSourceTag()` automatically appends the override-source debug tag to returned results
  - `Type` enum fully wired to `ConflictTag` constants
  - `DatapackOverrideReloader` fully loads `BUILTIN`/`CREATOR` overrides from datapacks
  - All three reload listeners registered in `Chord.java`
- **Remaining Gaps:**
  - No `UserOverrideManager` — `USER`/`PLAYER` level overrides cannot be persisted or loaded
  - No `builtin_overrides/*.json` data files seeded in resources
- **Impact:** Medium — automatic (BUILTIN/CREATOR) overrides work end-to-end; user-defined overrides are blocked only by missing persistence layer

#### 7. **Build Infrastructure**
- **Gradle Setup:** ✅ Complete - ModDevGradle 2.0.141 configured
- **Java 21 Enforcement:** ✅ Complete
- **Run Configs:** ✅ Autogenerated - runClient/runServer/runData/runGameTestServer
- **Parchment Mappings:** ✅ Configured - Enhanced parameter names available

---

### ⚠️ Partially Implemented Components

#### 1. **Intent System** (Intent.java)
- **Status:** 20% - Framework only
- **Current State:**
  - Interface with `name()` method and `of(String)` factory (returns anonymous lambda — no enum)
  - `hasShared()`: implemented via reference equality (`==`) — will always return false for `of()`-created instances since each call creates a new lambda
  - `contains()`: implemented via HashSet containsAll
  - `isIdentical()`: stub — hardcoded `return false`
- **Gaps:**
  - No concrete Intent implementations or enum; `hasShared()` reference-equality check means it never triggers in practice
  - No intent categories (movement, camera, ui, combat, etc.)
  - `isIdentical()` is non-functional
- **Impact:** Moderate - `evaluateIntent()` is called but effectively a no-op; the downgrade path for `Interceptive` risks is never reached via intent

#### 2. **Context Layer** (IKeyContext.java + KeyContext.java)
- **Status:** 70% - Functional for current use cases
- **Current State:**
  - `KeyContext` enum complete with 4 values: `AS_IS`, `UNIVERSAL`, `IN_GAME`, `IN_GUI`
  - Each value wraps a `UnaryOperator<IKeyConflictContext>` and implements `transform()`
  - `IKeyContext.Lookup` delegates to `KeyContext.valueOf(name)` — handles the 4 built-in contexts
- **Gaps:**
  - `Lookup.transform()` contains empty comment block suggesting planned reflection-based mod context lookup that was never implemented
  - No support for mod-provided `IKeyConflictContext` implementations beyond the 4 built-in values
- **Impact:** Low - sufficient for vanilla + NeoForge standard contexts; extensibility to arbitrary mod contexts is blocked

#### 3. **Mutex State Operations** (StateSet.java)
- **Status:** 85% - Core algebra functional, edge cases uncertain
- **Current State:**
  - `HyperRect` + `UnionSet` sealed implementations with full algebra
  - `intersect`, `union`, `complement`, `isSubsetOf`, `isProperSubsetOf`, `isSupersetOf` all implemented
  - `isIdenticalWith()` has a working double-subset check (despite `// TODO` comment)
  - `MutexSetManager` fully loads mutex group definitions from datapacks
- **Gaps:**
  - `HyperRect.intersect()` and `HyperRect.complement()` both have `// TODO recheck` markers — correctness of multi-dimension complement logic unverified by tests
  - No unit tests to validate algebra properties (commutativity, associativity, De Morgan's laws)
- **Impact:** Low-medium — logic appears sound but edge cases in complex multi-set boolean trees are unproven

---

### ❌ Missing/Incomplete Components

#### 1. **User Interface Layer (0% - Design Phase)**
- **Missing:**
  - Mod options/settings screen
  - Keybinding conflict visualization/reporting UI
  - Override management UI
  - In-game conflict warnings/notifications
- **Requirements:**
  - Screen class, list/tree widget for conflicts, severity coloring, override buttons

#### 2. **Testing Infrastructure (0%)**
- **Missing:**
  - No unit tests for Evaluator pipeline
  - No state algebra property tests
  - No integration tests with actual KeyMappings
  - No game tests (runGameTestServer not utilized)
- **Requirements:**
  - JUnit 5 test suite with parameterized tests for matrix combinations

#### 3. **Event Listeners & Game-Lifecycle Hooks (30%)**
- **Implemented:**
  - `RegisterClientReloadListenersEvent` — all 3 resource reload listeners registered in `Chord.java`
- **Missing:**
  - KeyMapping registration listener (scan for conflicts when mods add new keybindings)
  - Client tick event listener (periodic conflict re-scan)
  - Screen open event listener (context-specific warnings)
  - Mod load completion listener (trigger full conflict scan)

#### 4. **KeySemantic / Override JSON Data Files (0%)**
- **Missing:**
  - No `key_semantics/*.json` files in `src/main/resources`
  - No `builtin_overrides/*.json` files in `src/main/resources` (reloader is ready but has nothing to load)
  - No Minecraft vanilla keybinding semantics catalog

#### 5. **User Override Persistence (0%)**
- **Missing:**
  - No `UserOverrideManager` — `USER`/`PLAYER` level overrides have no load/save path
  - `OverrideManager.OVERRIDES` map for these types is always empty at runtime
- **Impact:** High — users cannot persist custom conflict resolutions across sessions

#### 6. **Logging & Diagnostics (25%)**
- **Status:** `MutexSetManager` and `DatapackOverrideReloader` have good info/warn/debug logging. `Evaluator` pipeline has no logging.
- **Missing:** Debug-level pipeline stage logging in Evaluator, risk escalation tracing, performance metrics

#### 7. **Performance Optimization (0%)**
- No caching of conflict check results
- Resource overlap algorithm (LCA path) calls `String.split()` and `of()` on every evaluation

#### 8. **Documentation & Examples (30%)**
- **Exists:** `doc/CONTRIBUTOR_GUIDE_zh-cn.md`, `doc/PIPELINE_zh-cn.md`, `AGENTS.md` (updated)
- **Missing:** English translations, API JavaDoc, example JSON files, user guide

---

## Development Roadmap

### Phase 1: Core Engine Stabilization (Weeks 1-3, Priority: Critical)

**Goal:** Bring evaluation pipeline to production-ready state with comprehensive testing.

#### 1.1 Fix TODO Markers
- [ ] **StateSet HyperRect.intersect() and complement() recheck** (StateSet.java)
  - Effort: 2 days
  - Verify multi-dimension complement produces correct HyperRect list
  - Add unit tests covering De Morgan's laws, complement of union, etc.
  - **Files:** `eval/mutex/StateSet.java`

- [ ] **Intent.hasShared() reference equality bug**
  - Effort: 0.5 days
  - `of(String)` creates a new lambda each call — `hasShared()` can never return true for dynamically created intents
  - Fix: intern/cache Intent instances by name, or switch to name-based equality
  - **Files:** `eval/intent/Intent.java`

- [ ] **Resource.overlaps() null safety + optimization**
  - Effort: 1 day
  - Handle null resources (currently throws NPE if either resource is null)
  - Cache resource path hierarchies if needed
  - **Files:** `eval/resource/Resource.java`

#### 1.2 Add Comprehensive Unit Test Suite
- [ ] Create `src/test/java/com/lnatit/chord/` test structure
  - Effort: 5 days
  - **Test Coverage:**
    - Evaluator pipeline: 8 stages × 3-5 test cases each = 40+ tests
    - State algebra: isSubsetOf, intersect, union, complement (10+ tests)
    - Matrix lookups: Modality all 16 combinations, RedirectMode all pairs (8+ tests)
    - Dynamic risk escalation: StateSubset, InterceptInput, RaceCondition (6+ tests)
    - Codecs: JSON parsing for all 7 dimensions (7+ tests)
  - **Tools:** JUnit 5 + Parameterized tests + Mockito for KeyMapping
  - **Files:** `src/test/java/com/lnatit/chord/{eval,result,data}/*Test.java`

#### 1.3 Pipeline Integration Test
- [ ] Test Evaluator.conflicts() end-to-end
  - Effort: 2 days
  - **Files:** `src/test/java/com/lnatit/chord/eval/EvaluatorIntegrationTest.java`

#### 1.4 Documentation
- [ ] Translate PIPELINE_zh-cn.md to English — `doc/PIPELINE.md` (new)
- [ ] Write API JavaDoc for KeySemantic and all 7 dimension classes

**Milestone:** Evaluator pipeline verified production-ready, test coverage >80%.

---

### Phase 2: Intent System Implementation (Weeks 4-5, Priority: High)

**Goal:** Complete the Intent dimension with semantic categorization and matching.

#### 2.1 Define Intent Categories
- [ ] Create Intent enum with predefined categories
  - **Proposed Categories:** MOVEMENT, CAMERA, INTERACTION, UI, UTILITY
  - **Files:** `eval/intent/Intent.java` (enum implementation)

#### 2.2 Fix Intent Matching Logic
- [ ] Fix `Intent.hasShared()` — reference equality (`==`) never fires for `of()`-created instances; intern by name or use enum
- [ ] Implement `Intent.isIdentical()` — currently hardcoded `return false`
- [ ] Add semantic similarity for intent downgrading in `evaluateIntent()`
- **Files:** `eval/intent/Intent.java`

#### 2.3 Intent JSON Codec
- [ ] Test `INTENT_CODEC` in Codecs.java — deserialize names to Intent instances and validate
- **Files:** `data/Codecs.java`

#### 2.4 Sample Intent Definitions
- [ ] Create `src/main/resources/data/chord/key_semantics/minecraft.json` with vanilla key intents
- **Files:** `src/main/resources/data/chord/key_semantics/minecraft.json` (new)

**Milestone:** Intent system functional, `evaluateIntent()` produces meaningful downgrade results.

---

### Phase 3: Override Manager & User Configuration (Weeks 6-8, Priority: High)

**Goal:** Complete user-level override persistence; seed builtin override data files.

> **Note:** Phase 3.1 (in-memory registry), 3.3 (builtin reloader), and 3.5 (debug tagging) are **already complete**. Only 3.2 and 3.4 remain.

#### 3.2 User Override Persistence ⬅ Remaining gap
- [ ] Create `UserOverrideManager` — load/save `USER`/`PLAYER` overrides from `config/chord/user_overrides.json`
  - Effort: 3 days
  - Populate `OverrideManager.OVERRIDES` for `Type.USER` / `Type.PLAYER` on game start
  - Save on config update
  - **Files:** `data/override/UserOverrideManager.java` (new)

#### 3.4 Builtin Override Registry ⬅ Remaining gap
- [ ] Seed `src/main/resources/data/chord/builtin_overrides/` with documented JSON files
  - Effort: 2 days
  - Research common mod conflicts; `DatapackOverrideReloader` is ready — just needs data
  - **Files:** `src/main/resources/data/chord/builtin_overrides/*.json` (new)

#### ~~3.1 Override Storage Backend~~ ✅ Done
#### ~~3.3 Builtin Override Reloader~~ ✅ Done (`DatapackOverrideReloader` fully implemented)
#### ~~3.5 Override Debug Tagging~~ ✅ Done (`ConflictTag` has all 4 override tags; `Type` enum + `withSourceTag()` wired)

**Milestone:** Users can persist custom conflict resolutions; builtin registry seeded.

---

### Phase 4: Event Listeners & Real-Time Conflict Detection (Weeks 9-11, Priority: Medium)

**Goal:** Integrate conflict detection into game lifecycle.

> **Note:** `RegisterClientReloadListenersEvent` is already handled in `Chord.java`. Phase 4.3 is complete.

#### 4.1 Client Event Handler Setup
- [ ] Create `ChordClientEvents.java` — `@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)`
  - **Files:** `ChordClientEvents.java` (new)

#### 4.2 KeyMapping Registration Listener
- [ ] Hook `RegisterKeyMappingsEvent` — scan for conflicts on keybind registration, cache results
  - **Files:** `ChordClientEvents.java`

#### ~~4.3 Resource Reload Listener~~ ✅ Done (all 3 reload listeners registered in `Chord.java`)

#### 4.4 Client Tick Listener (Optional)
- [ ] Periodic conflict re-scan every N ticks
  - **Files:** `ChordClientEvents.java`

**Milestone:** Chord detects conflicts as mods load.

---

### Phase 5: User Interface (Weeks 12-16, Priority: Medium)

**Goal:** Present conflict information and allow users to manage resolutions in-game.

#### 5.1 Mod Options Screen
- [ ] `ChordConfigScreen` — conflict list with severity coloring, filter buttons, search
  - **Files:** `client/ChordConfigScreen.java` (new)

#### 5.2 Conflict Detail View
- [ ] `ConflictDetailScreen` — 7 dimensions side-by-side, primary risk explanation
  - **Files:** `client/ConflictDetailScreen.java` (new)

#### 5.3 Override Management UI
- [ ] Override buttons (Mark SAFE / Mark WARNING / Ignore / Reset) wired to `UserOverrideManager`
  - **Files:** `client/ChordConfigScreen.java` (extend)

#### 5.4 In-Game Notification System
- [ ] Toast/HUD for SEVERE conflicts on mod load
  - **Files:** `client/ChordNotificationHandler.java` (new)

#### 5.5 Localization (i18n)
- [ ] `src/main/resources/assets/chord/lang/en_us.json` (new)

**Milestone:** Users can view, understand, and manage conflicts via in-game GUI.

---

### Phase 6: Testing & Documentation (Weeks 17-18, Priority: Medium)

**Goal:** Full test coverage, user documentation, and contributor guides.

#### 6.1 Integration Tests
- [ ] `ConflictDetectionIT.java` — 5+ end-to-end scenarios

#### 6.2 Game Tests
- [ ] `ChordGameTests.java` — verify mixin injection in real game context

#### 6.3 JSON Schema Documentation
- [ ] `doc/KEY_SEMANTICS_FORMAT.md` — field-by-field JSON format guide

#### 6.4 User Guide
- [ ] `doc/USER_GUIDE.md` — reading severity levels, using config screen, creating overrides

#### 6.5 Contributor Guide Update
- [ ] `doc/CONTRIBUTOR_GUIDE.md` — English translation + expanded semantic definition guide

**Milestone:** Test coverage >85%, all documentation in English, ready for beta release.

---

### Phase 7: Performance & Polish (Weeks 19-20, Priority: Low)

#### 7.1 Conflict Check Caching
- [ ] `eval/ConflictCache.java` — LRU cache keyed by (key1, key2, semantic snapshot)

#### 7.2 Performance Profiling
- [ ] Benchmark 50/100/200 keybindings, target <10ms per conflict pair

#### 7.3 Code Cleanup
- [ ] Resolve remaining `// TODO recheck` markers in `StateSet.java`
- [ ] Remove deprecated `INTENT_SHARED` tag in `ConflictTag.java`
- [ ] Standardize logging levels across all managers

#### 7.4 Dependency Review
- [ ] Audit transitive dependencies for CVEs; update if needed

**Milestone:** Clean, performant, production-ready codebase.

---

### Phase 8: Future Enhancements (Post-Release, Priority: Low)

#### 8.1 Advanced Features
- [ ] Conflict prediction, mod compatibility matrix, profile system

#### 8.2 Integrations
- [ ] EMI/REI, JourneyMap, Xaero's Map, Quark, Controlling

#### 8.3 Accessibility
- [ ] Screen reader support, high contrast mode, colorblind-friendly indicators

---

## Success Criteria & Metrics

### Functional Completeness
- [ ] All 7 dimensions integrated and tested
- [ ] 8-stage pipeline verified with edge cases
- [ ] User override system fully functional
- [ ] GUI accessible and intuitive
- [ ] Documentation complete in English

### Quality Metrics
- [ ] Test coverage: >85% (line coverage) + >80% (branch coverage)
- [ ] Critical bugs: 0 (severity = SEVERE issues)
- [ ] Performance: <10ms per conflict pair (cached)
- [ ] Memory: <50MB typical usage with 100 keybindings

### User Adoption
- [ ] Documentation clarity score: >4/5 (peer review)
- [ ] First-time user success rate: >90% (can identify own conflicts)
- [ ] Community feedback: Track GitHub issues/discussions

### Code Quality
- [ ] Sonarqube: No code smells, <5% duplication
- [ ] Javadoc: 100% coverage of public API
- [ ] Build: Green CI/CD pipeline (if applicable)

---

## Risk Mitigation

### Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| State algebra edge cases cause false positives | Medium | High | Phase 1: Comprehensive unit tests + property-based testing |
| Intent categorization too rigid | Medium | Medium | Phase 2: Design intent categories with community input, allow custom intents later |
| Performance degradation with 200+ keybinds | Low | Medium | Phase 7: Caching + profiling early, incremental optimization |
| Mixin conflicts with other mods | Low | High | Phase 1: Test with popular mods (Controlling, etc.), document compatibility |

### Resource Risks

| Risk | Mitigation |
|------|-----------|
| Large time commitment (20 weeks) | Break into smaller phases; Phase 1-3 are critical path; later phases can be parallel |
| Lack of testing expertise | Use parameterized tests for matrix combinations; leverage existing test patterns |
| Chinese documentation barrier | Machine translation + peer review for initial translation; community help for refinement |

---

## Timeline Summary

```
Phase 1: Core Stabilization          (Weeks 1-3)   [CRITICAL PATH]
Phase 2: Intent Implementation       (Weeks 4-5)   [CRITICAL PATH]
Phase 3: Override Manager            (Weeks 6-8)   [CRITICAL PATH — only 3.2 & 3.4 remain]
Phase 4: Event Listeners             (Weeks 9-11)  [Parallel possible — 4.3 already done]
Phase 5: User Interface              (Weeks 12-16) [Parallel with Phase 4]
Phase 6: Testing & Documentation     (Weeks 17-18) [Final QA]
Phase 7: Performance & Polish        (Weeks 19-20) [Final polish]
────────────────────────────────────────────────────
Estimated Total: 20 weeks (5 months) for MVP (Phases 1-6)
Extended Timeline: 20 weeks for production-ready (Phases 1-7)
```

---

## Decision Points for Review

1. **Intent Categories:** Proposed 5 categories (Movement, Camera, Interaction, UI, Utility). Allow custom?
2. **Override Scope:** Only keybind pairs, or include partial overrides (override one dimension only)?
3. **UI Framework:** SimpleConfigScreen or custom Screen implementation?
4. **Performance Target:** <10ms per pair (cached), or acceptable to aim lower initially?
5. **Release Strategy:** Beta as Phase 6 MVP, or wait for Phase 7 polish?
6. **Intent equality:** Intern `Intent` instances by name (HashMap cache) or switch to enum? Enum is simpler but less extensible for mod-defined intents.

---

## File Structure Reference

```
src/main/java/com/lnatit/chord/
├── Chord.java                           [DONE - 3 reload listeners registered]
├── eval/
│   ├── Evaluator.java                   [DONE - 90%, interface with static methods]
│   ├── KeySemantic.java                 [DONE - DEFAULT constant present; add JavaDoc]
│   ├── SemanticalKey.java               [DONE]
│   ├── Modality.java                    [DONE - 95%]
│   ├── RedirectMode.java                [DONE - 95%]
│   ├── override/
│   │   ├── OverrideManager.java         [DONE - EnumMap storage, priority, withSourceTag()]
│   │   └── Type.java                    [DONE - wired to ConflictTag constants]
│   ├── context/
│   │   ├── IKeyContext.java             [70% - Lookup works for built-in 4 contexts only]
│   │   └── KeyContext.java              [DONE - AS_IS/UNIVERSAL/IN_GAME/IN_GUI]
│   ├── intent/
│   │   └── Intent.java                  [Phase 2 - hasShared() broken, isIdentical() stub]
│   ├── mutex/
│   │   ├── StateSet.java                [85% - 2x TODO recheck; algebra functional]
│   │   └── tree/                        [DONE - And/Or/Not/Leaf nodes]
│   └── resource/
│       └── Resource.java                [80% - TODO, null safety needed]
├── data/
│   ├── Codecs.java                      [DONE - all codecs including tree, override, mutex]
│   ├── Requirement.java                 [DONE]
│   ├── mutex/
│   │   ├── MutexSetManager.java         [DONE - fully operational]
│   │   ├── MutexSet.java                [DONE]
│   │   └── MutexDefinition.java         [DONE]
│   ├── semantic/
│   │   ├── KeySemanticManager.java      [DONE]
│   │   └── KeyDefinitions.java          [DONE]
│   └── override/
│       ├── DatapackOverrideReloader.java [DONE - fully operational]
│       ├── OverrideDefinition.java       [DONE]
│       └── UserOverrideManager.java      [Phase 3.2 - NEW]
├── result/                              [DONE - 95%+]
│   ├── ConflictResult.java              [DONE - SAFE constant]
│   ├── ConflictTag.java                 [DONE - all tags including 4 override-source tags]
│   ├── ConflictRisk.java                [DONE - interface + Static inner class]
│   ├── DynamicRisk.java                 [DONE - all 7 concrete subclasses implemented]
│   ├── Severity.java                    [DONE]
│   └── ConflictCollector.java           [DONE]
├── mixin/client/
│   └── MixinKeyMapping.java             [DONE]
├── util/                                [DONE]
│   ├── AsymmetricEnumMatrix.java
│   ├── Provider.java
│   └── Supplier.java
└── client/                              [Phase 5 - NEW]
    ├── ChordClientEvents.java           [Phase 4]
    ├── ChordConfigScreen.java           [Phase 5]
    ├── ConflictDetailScreen.java        [Phase 5]
    └── ChordNotificationHandler.java    [Phase 5]

src/test/java/com/lnatit/chord/         [Phase 1-2 - NEW]
├── eval/
│   ├── EvaluatorTest.java
│   ├── StateSetTest.java
│   └── EvaluatorIntegrationTest.java
├── result/
│   ├── DynamicRiskTest.java
│   └── ModalityMatrixTest.java
└── data/
    └── CodecsTest.java

src/main/resources/data/chord/
├── key_semantics/
│   └── minecraft.json                   [Phase 2 - NEW]
├── builtin_overrides/                   [Phase 3.4 - NEW (reloader ready, no data yet)]
│   └── common_conflicts.json
└── mutex_sets/                          [Phase 3.4 - NEW (manager ready, no data yet)]

src/main/resources/assets/chord/lang/
└── en_us.json                           [Phase 5 - NEW]

doc/
├── PIPELINE.md                          [Phase 1 - new translation]
├── CONTRIBUTOR_GUIDE.md                 [Phase 1 - translate + Phase 6 expand]
├── USER_GUIDE.md                        [Phase 6 - NEW]
├── KEY_SEMANTICS_FORMAT.md              [Phase 6 - NEW]
├── PERFORMANCE_REPORT.md                [Phase 7 - NEW]
└── AGENTS.md                            [✅ Updated]
```

---

## Conclusion

This roadmap provides a structured path to transform Chord from a powerful backend engine into a complete, user-facing mod. The critical path (Phases 1-3) focuses on correctness and user configuration, while later phases add polish and features. The phased approach allows for early validation and course correction before investment in UI/documentation.

**Next immediate action:** Phase 1 — fix `Intent.hasShared()` reference equality bug, add `Resource.overlaps()` null safety, and begin unit test infrastructure for `StateSet` algebra.

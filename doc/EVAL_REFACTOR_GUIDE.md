# eval 包重构指导文档

> **约束：** 不修改其他包；`Evaluator` 执行流程与功能不变；不调用任何 `legacy` 包路径下的代码；
> 适配新 `semantic`（`KeySemantic` / `KeyContext` / `ContextSemantic`）和新 `result`（`RiskEntry` / `ContextCollector` / `Finalized`）。

---

## 一、重构前置约定（本轮新增）

1. 进入冲突判定前，先调用 `SemanticalKey::chord$compareTo` 做按键规范化排序：
   - 若 `left > right`，交换为 `left <= right`。
   - 后续所有阶段与局部变量统一使用 `left/right`，不再使用 `subject/opponent` 作为主语义名。
2. 旧 `DynamicRisk` 的“动态严重度变化”由 `RiskEntry.Simple<T>` 承接。
3. 对于需要额外参数的旧动态风险（如方向位 `subjectIsSubset` / `subjectIsModifier`），使用专用 `RiskEntry` 子类承接参数，**不要**再向 `ContextCollector` 扩字段。
4. 旧 `ConflictCollector.finish()` 模式废弃；是否提前熔断由 `evaluateXxx(...)` 返回 `boolean` 控制。

---

## 二、现状诊断

### 2.1 eval 包 legacy 依赖现状

| 文件 | legacy 依赖 | 需要改动 |
|------|------------|---------|
| `eval/Evaluator.java` | `legacy.SemanticalKey`, `legacy.ConflictResult`, `legacy.ConflictCollector`, `legacy.DynamicRisk`, `legacy.ConflictInfo`, `legacy.ContextPair` | **大改** |
| `eval/Modality.java` | `legacy.ConflictRisk` | **中改** |
| `eval/RedirectMode.java` | `legacy.ConflictInfo`, `legacy.ConflictRisk`, `legacy.DynamicRisk` | **大改** |
| `eval/override/OverrideManager.java` | `legacy.ConflictResult`, `legacy.ConflictRisk` | **中改** |
| `eval/context/*` | 与 `semantic.KeyContext` 职责重叠 | **删除** |

### 2.2 新系统类型对照

| 旧 legacy 类型 | 新类型 | 位置 |
|--------------|--------|------|
| `ConflictResult` | `ConflictResult(left, right, Finalized)` | `result/ConflictResult.java` |
| `ConflictCollector.Context` | `ContextCollector` | `result/context/ContextCollector.java` |
| `ConflictCollector.Meta` | `Collector.pipeline()` | `result/Collector.java` |
| `DynamicRisk` | `RiskEntry.Simple<T>` + 专用 `RiskEntry` 子类 | `result/` |
| `ConflictTag` | 分阶段 tag enum | `result/context/*Tag.java` |

---

## 三、动态风险迁移规则（修订）

### 3.1 Simple 与 Static 是否需要区分

本轮建议：
- **可变条目**：`RiskEntry.Simple<T>`（允许 `setSeverity`）。
- **固定条目**：继续用 `RiskEntry.Diagnostic`（SAFE）或不可变 `RiskEntry` 实现。
- 不强制新增 `Static` 类型；若后续出现大量“固定且非 SAFE”条目，再统一引入 `RiskEntry.Static<T>`。

### 3.2 额外参数承载方式

旧动态对象里携带的参数，不再塞进 `ContextCollector` 字段，统一改为专用条目类型：

- `StateSubsetEntry extends RiskEntry.Simple<StateTag>`
  - 字段：`boolean leftIsSubset`
- `RedirectModifierEntry extends RiskEntry.Simple<RedirectTag>`
  - 字段：`boolean leftIsModifier`
  - 能力：供 Modality 阶段按方向重算 severity

> 说明：这些类型定义属于 `result` 契约层；本轮 eval 改造以“调用这些类型”为前提，不在本轮改动其他包。

---

## 四、阶段实现规格（保持流程不变）

### 4.1 顶层入口

1. 先规范化 `left/right`。
2. `isSameKey(left, right)`。
3. override 查询。
4. context 路由。
5. Stage4~8 语义流水线。

### 4.2 Stage4~8 的熔断控制

所有阶段统一签名：

```java
static boolean evaluateXxx(...)
```

返回规则：
- `true`：继续下一阶段
- `false`：提前结束当前 context 流水线

主流程：

```java
if (!evaluateStateMutex(...)) return collector;
if (!evaluateIntercept(...)) return collector;
if (!evaluateRedirect(...)) return collector;
evaluateResource(...);
evaluateIntent(...);
evaluateModality(...);
```

### 4.3 Stage 细则

- `evaluateStateMutex`
  - 互斥：写 `STATE_MUTEX`，返回 `false`
  - 子集：写 `StateSubsetEntry`（含 `leftIsSubset`），返回 `true`
  - 其余：写 `STATE_INTERSECT`，返回 `true`

- `evaluateIntercept`
  - 若发现 `StateSubsetEntry` 且命中升级条件：升级为 `PARTIAL_OVERRIDE`，返回 `false`
  - 双拦截：`RACE_CONDITION`
  - 单拦截：`INTERCEPT_INPUT`
  - 无拦截：`CONCURRENT_INPUT`
  - 常规返回 `true`

- `evaluateRedirect`
  - 使用新 `RedirectInfo`（`tag/initSeverity/meltdown/modalDependent`）
  - 若 `meltdown=true` 返回 `false`，否则返回 `true`

- `evaluateResource`
  - 纯固定条目写入，读取当前 intercept 条目判断是否拦截保护

- `evaluateIntent`
  - 若存在可降级拦截条目，直接 `downgrade`
  - 否则写 `INTENT_SHARE/INTENT_IRRELEVANT`

- `evaluateModality`
  - 先处理 redirect 的模态依赖重算
  - 再写 modality 矩阵条目

---

## 五、矩阵重构规格

### 5.1 `Modality.MATRIX`

- 目标类型：`AsymmetricEnumMatrix<Modality, Supplier<RiskEntry.Simple<ModalityTag>>>`
- 每次 `get()` 必须返回新对象，避免跨评估共享可变状态。

### 5.2 `RedirectMode.MATRIX`

替代 `ConflictInfo`：

```java
record RedirectInfo(RedirectTag tag, Severity initSeverity, boolean meltdown, boolean modalDependent) {}
```

映射保持原语义不变：
- `NONE+NONE -> NO_REDIRECT SAFE`
- `KEY+NONE -> CONTEXT_LEAK (modalDependent)`
- `KEY+KEY -> DEFERRED_RISK (modalDependent)`
- `MOUSE+NONE -> LOSE_FOCUS (modalDependent)`
- `KEY+MOUSE -> INPUT_BLOCK (modalDependent)`
- `MOUSE+MOUSE -> FOCUS_COLLISION (meltdown)`
- default -> `CONTEXT_CLASH (meltdown)`

---

## 六、Context 路由与 KeySemantic 适配

- `Semantical + Semantical`：进入语义流水线（笛卡尔积）。
- 任一侧 `RawContext`：走 `CONTEXT_DIRECT`（本轮仅保留入口，不改 Context 包实现）。
- overlap 判断统一基于 `KeyContext.context()` 的 `conflicts()`。

---

## 七、OverrideManager 迁移

- `getOverride(left, right)` 返回新 `Finalized` 或 `null`。
- 不再注入 legacy source tag。
- 未来来源区分由 `ConflictResult.origin` 负责（见第八节）。

---

## 八、`ConflictResult.origin` 未来规范（新增）

后续添加 `origin` 字段时遵循以下约束：

1. **枚举域固定**：`PIPELINE`, `CONTEXT_DIRECT`, `SHORT_CIRCUIT_SAFE`, `OVERRIDE`。
2. **单结果单来源**：一个 `ConflictResult` 仅有一个主 `origin`。
3. **可追溯**：当 `origin=OVERRIDE` 时，补充 `overrideType`（`USER/PLAYER/CREATOR/BUILTIN`）。
4. **展示优先级**：
   - `OVERRIDE` 最高；
   - 其次 `PIPELINE`；
   - 再次 `CONTEXT_DIRECT`；
   - `SHORT_CIRCUIT_SAFE` 最低。
5. **兼容策略**：旧数据默认映射为 `PIPELINE`，不得出现 `null` origin。

---

## 九、文件级改动清单（eval-only）

| 文件 | 操作 |
|------|------|
| `eval/Evaluator.java` | 全量重写：去 legacy、改 left/right、改 bool-return 熔断 |
| `eval/Modality.java` | MATRIX 改为 Supplier of `RiskEntry.Simple<ModalityTag>` |
| `eval/RedirectMode.java` | 引入 `RedirectInfo`，移除 `ConflictInfo`/`DynamicRisk` |
| `eval/override/OverrideManager.java` | 返回新 `Finalized`，去 legacy 引用 |
| `eval/context/*` | 删除（功能已由 `semantic.KeyContext` 承接） |

---

## 十、执行顺序建议

1. 先改 `Modality` / `RedirectMode` 矩阵类型。
2. 再改 `OverrideManager` 返回类型。
3. 最后重写 `Evaluator`（left/right + bool-return 熔断 + no legacy）。
4. 删除 `eval/context/*`。
5. 自检：`eval` 包 import 不得包含 `legacy`。

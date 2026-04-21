# Context 语义模式重构设计（PRECISE / ASIS）

## 1. 背景与问题

当前 `Evaluator` 在上下文阶段采用“笛卡尔积 + `conflicts()` 双向判断”：

- 外层遍历 `subject` 的所有语义 context
- 内层遍历 `opponent` 的所有语义 context
- 逐对调用 `isContextOverlapping(a, b)`

在中大型整合包里，这会产生大量“注定不重叠”的 Pair 计算。根据对公开模组的观察：

- 大多数 `IKeyConflictContext#conflicts` 复写属于
  - 一致性判断：`other == this`
  - 或“恒不冲突”：`false`
- 少部分是归并性判断（例如 `other == this || other == IN_GAME`），其行为在本系统语义上可视为 `IN_GAME` 类别
- 只有极少数模组实现复杂自定义冲突逻辑

结论：继续把上下文关系完全交给运行时 `conflicts()` 探测，收益低、开销敏感且不可控。

---

## 2. 设计目标

1. **性能优先**：减少无效 Pair 枚举，降低 `Evaluator.conflicts(...)` 热路径耗时。
2. **语义可控**：把上下文重叠关系从“隐式行为推导”改为“可配置语义”。
3. **兼容保底**：为少数复杂模组保留原生冲突逻辑路径。
4. **可扩展**：支持后续通过数据包新增 context 语义类型，而非硬编码在 `KeyContext`。

### 非目标

- 不追求完全自动推断所有第三方 `IKeyConflictContext` 语义
- 不尝试在首版覆盖所有复杂自定义 context 逻辑

---

## 3. 核心方案

将现有 `KeyContext` 语义拆为两种指定模式：

## 3.1 PRECISE 模式（推荐默认）

PRECISE 表示“显式语义声明”，不依赖 `conflicts()` 行为推断。

- 内建基础语义：`UNIVERSAL` / `IN_GAME` / `IN_GUI`
- 支持通过数据包注册新语义类别（如“只与自身冲突”“与 IN_GAME 归并”“永不冲突”）
- 在按键语义配置中**禁止或不推荐使用 `UNIVERSAL`**（降低跨域冲突面）

PRECISE 的重叠关系由配置或内建规则直接决定，不走 `IKeyConflictContext#conflicts`。

## 3.2 ASIS 模式（兼容兜底）

ASIS 表示“保留原始上下文行为”。

- 使用原始 `IKeyConflictContext` 对象
- 重叠判定沿用当前逻辑：
  - `a.conflicts(b) || b.conflicts(a)`

ASIS 用于少数实现复杂逻辑的模组或无法建模的场景。

---

## 4. 数据模型建议

建议在 `eval.context` 引入包装对象（可与现有 TODO 对齐）：

```java
interface ContextDescriptor extends Comparable<ContextDescriptor> {
    String id();              // 稳定标识（序列化/比较）
    String translationKey();  // 文本展示
    Mode mode();              // PRECISE / ASIS
}
```

其中：

- PRECISE 描述符额外包含“语义类别”与“重叠策略”
- ASIS 描述符携带原始 `IKeyConflictContext` 引用（或可解析句柄）

### 重叠策略建议（PRECISE）

可先提供有限策略，避免过度通用化：

- `SELF_ONLY`：仅与同类别冲突
- `NEVER`：不冲突
- `UNIVERSAL`：与全部冲突

这能覆盖你调研中的主流实现模式。

---

## 5. Evaluator 改造点

现有逻辑入口：`Evaluator.isContextOverlapping(...)`。

改造后建议：

1. 若两侧 descriptor 均为 PRECISE：使用策略表直接判断（O(1)）
2. 只要任一侧为 ASIS：回退到 `conflicts()` 双向判断

这样可以把绝大多数主流场景走到快速路径，同时保留复杂模组兼容性。

---

## 6. Pair 设计影响

该方案与当前“有向 `ContextPair`”并不冲突：

- `ContextPair` 继续作为结果层表达（记录具体语义组合）
- 优化的是“如何筛出有效 Pair”，而不是删除 Pair

即：减少无效 Pair 枚举，保留有效 Pair 的可解释性。

---

## 7. 配置与数据包草案

建议新增一类数据包目录（示意）：

`data/chord/context_registry/*.json`

示例（草案）：

```json
{
  "id": "in_game",
  "translation_key": "chord.context.in_game",
  "mode": "PRECISE",
  "policy": "SELF_ONLY"
}
```

归并示例：

```json
{
  "id": "ingame_like",
  "translation_key": "chord.context.ingame_like",
  "mode": "PRECISE",
  "policy": "MERGE_IN_GAME"
}
```

ASIS 示例：

```json
{
  "id": "raw_mod_ctx",
  "translation_key": "chord.context.raw_mod_ctx",
  "mode": "ASIS"
}
```

---

## 8. 性能预期

在“主流 context 实现分布”成立前提下：

- PRECISE 命中率高时，可显著减少 `conflicts()` 调用次数
- 无效 Pair 筛选成本从“运行时行为判断”前移为“策略常量判断”
- 对中大型整合包，预计在上下文阶段有可观收益

注：最终收益仍取决于每个按键的语义条目数与 PRECISE 覆盖率。

---

## 9. 兼容性与迁移策略

建议分阶段推进：

1. **阶段A**：引入 descriptor 与双模式，不改现有语义文件（默认 ASIS 兼容）
2. **阶段B**：为内建语义和常见模式补充 PRECISE 注册
3. **阶段C**：在内容规范中标注“禁用/不推荐 UNIVERSAL”，并逐步迁移 key semantics
4. **阶段D**：基于 profiler 数据评估覆盖率和收益，再决定是否继续压缩 ASIS 使用范围

---

## 10. 风险与约束

- 错误的 PRECISE 策略映射会导致漏报/误报
- ASIS 仍需保留，不能强行移除
- 数据包作者需要理解策略语义，文档和校验（codec）必须同步完善

---

## 11. 结论

该方案**可行且适合当前项目阶段**：

- 在不牺牲兼容性的前提下，把主流场景从动态推断迁移到显式语义
- 与现有 Evaluator 主体逻辑兼容，改造边界清晰
- 对“eval 时间敏感”的整合包场景有现实价值

建议按“ASIS 保底 + PRECISE 增量覆盖”的路径落地。


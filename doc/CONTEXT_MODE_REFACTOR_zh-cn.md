# Context 系统重构设计（Result 优先阶段）

> 本文更新为“设计参考版”：当前迭代先落地 `result` 包重构，`Context` 改造仅定义规则与接口边界。

---

## 1. 本轮范围与前提

### 1.1 范围

- **本轮落地目标**：`result` 包重构。
- **本轮非落地目标**：Context 路由与分桶实现细节（仅定规则，后续实现）。

### 1.2 关键前提

- `Evaluator` 主流水线结构保持不变。
- Context 层必须保留，因为其承担外部 `IKeyConflictContext` 适配逻辑。
- Result 层只表达“已路由后的结论”，不承担复杂 Context 推理。

---

## 2. IKeyConflictContext 三分类模型

> 该分类用于路由决策，不是对 NeoForge API 的替换。

## 2.1 第一类：自限性 Context（Self-Limited）

定义：冲突条件为真仅当对象为自身（扩展自限类型可并入同类处理）。

特征：

- 仅这一类支持按键语义指定。
- 支持一个按键绑定多个 Context。
- 支持 Per-Context 语义。

处理：

- 进入语义流水线（State/Intercept/Redirect/Resource/Intent/Modality）。
- 原有细粒度语义判断能力保留在此类内部。

## 2.2 第二类：全局或复杂条件 Context（Global/Complex）

定义：冲突条件依赖复杂运行逻辑，无法稳定映射到自限语义。

特征：

- 无语义指定需求。
- 现实中多数“非推荐 UNIVERSAL 写法”可归入此类。

处理：

- 与第一类比较时，直接将该原始 Context 与第一类按键已指定的所有 Context 逐一比较。
- 根据比较结果直接产出结论。
- **不进入语义流水线**。

## 2.3 第三类：恒为假 Context（Always-False）

定义：冲突判定恒为 `false`。

处理：

- 比较时直接判定不冲突。
- 走短路路径，避免额外计算。

---

## 3. 路由规则（设计约束）

为避免行为歧义，路由优先级建议固定如下：

1. 任一侧为第三类（Always-False） -> 直接 `SAFE`，短路返回。
2. 存在第二类（Global/Complex）与第一类（Self-Limited）组合 -> 走“逐一原始比较”路径，直出结论。
3. 两侧均为第一类（Self-Limited） -> 进入语义流水线。
4. 第二类与第二类比较 -> 走原始比较路径，是否输出细节由 Result 侧策略决定。

说明：

- 这里的“逐一原始比较”指基于 `IKeyConflictContext#conflicts` 的直接比较，不混入语义层推导。

---

## 4. Context 层为何必须保留

Context 层继续承担以下职责：

- 外部 `IKeyConflictContext` 分类与路由。
- 第一类语义指定的索引与展开（多 Context / Per-Context）。
- 第二类原始比较的调度。
- 第三类短路出口。

Result 层仅消费 Context 层输出的“已决策事实”，避免职责污染。

---

## 5. 对 Result 设计的直接影响

## 5.1 Result 需要三种来源形态

建议将结果来源显式化为三类：

1. `SEMANTIC_PIPELINE`：第一类内部语义流水线结果（原先复杂结果模型主要迁移到这里）。
2. `CONTEXT_DIRECT`：第二类直判结果（不走语义流水线）。
3. `SHORT_CIRCUIT_SAFE`：第三类或其他短路安全结果。

可使用统一对外结构，但必须包含来源标记（origin）。

## 5.2 Hardware/Context 标签裁剪

建议：

- 不再默认对 Hardware/Context 阶段打调试标签。
- 仅在 `SEMANTIC_PIPELINE` 路径保留必要标签。
- 对 `CONTEXT_DIRECT` 和 `SHORT_CIRCUIT_SAFE` 路径以“来源 + 说明”替代标签堆叠。

目标：减少冗余标签噪声，提升结果可读性。

## 5.3 UserOverride 简化方向

建议把 UserOverride 收敛为：

- 用户提供说明文本（reason/comment）
- 用户声明期望结果级别（至少包含目标结论）

不再要求用户按“每层每标签”精确覆盖。

注意：

- 即使简化，也应保留最小机器可读字段（目标键对、目标结论、说明文本），避免完全不可审计。

---

## 6. 方案评估

## 6.1 优点

- 语义边界更清晰：可语义化与不可语义化场景分离。
- 性能更可控：第二/三类走直判或短路，减少无效流水线执行。
- 重构顺序合理：先稳定 Result 模型，再落地 Context 改造。
- 结果可解释性更好：可直接回答“该结论来自哪条路径”。

## 6.2 风险

- 三来源结果增加消费端分支复杂度（UI/日志/导出）。
- 第二类直判若无足够来源信息，后续排错会困难。
- 第一类多 Context + Per-Context 组合可能膨胀，需要后续限流策略。
- UserOverride 过度简化可能无法覆盖高级玩家诉求。

## 6.3 必要修正

建议在 Result 最小模型中加入：

- `origin`（来源）
- `explanation`（可读说明）
- `severity`（最终级别）

可选：

- `evidence`（原始比较证据摘要）

---

## 7. 可立即采纳 / 需验证项

## 7.1 可立即采纳

- 三分类模型（Self-Limited / Global-Complex / Always-False）。
- Context 层保留并承担路由逻辑。
- Result 来源三形态化。
- Hardware/Context 冗余标签裁剪方向。
- UserOverride 文本化简化方向（保留最小机器可读字段）。

## 7.2 需实验验证

- 第二类“直判不进流水线”在真实模组组合下的准确率。
- 标签裁剪后问题定位效率是否下降。
- 三来源结果对消费端代码复杂度影响。

## 7.3 最小验证清单

1. 构建三分类样例集（纯第一类、第一/第二混合、第三类短路）。
2. 对比新旧结论一致性（冲突与严重度）。
3. 记录 `conflicts()` 调用次数变化。
4. 验证结果展示可区分三来源且无歧义。
5. 验证 UserOverride 文本方案是否满足基本可解释需求。

---

## 8. 与 result 包重构的协同落地顺序

1. 先定义新的 `ConflictResult` 最小协议（origin/severity/explanation）。
2. 再定义三来源结果装配器（semantic/direct/short-circuit）。
3. Context 改造落地时，仅对装配输入做替换，不改 Result 对外契约。

这样可以把 Context 改造风险与 Result 重构风险解耦。

---

## 9. 当前结论

该方案总体可行，且适合当前“Result 优先”阶段：

- Context 分类与路由规则已足够清晰，可作为后续实现输入。
- Result 层可先完成来源建模与结构瘦身，再逐步接入 Context 新路径。
- 相比继续在单一标签体系内堆规则，三来源模型更稳定、更易维护。

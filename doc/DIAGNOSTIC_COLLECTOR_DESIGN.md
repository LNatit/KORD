# 诊断收集器设计（与 Context 三分类对齐版）

> 本文替换旧版“统一标签收集”叙述，改为“按来源收集 + 最小结果协议”。
> 当前迭代以 `result` 包重构为主，Context 改造仅作为输入约束。

---

## 1. 本轮范围与强约束

### 1.1 范围

- **落地目标**：重构 `result` 包与诊断收集器。
- **参考约束**：采用 Context 三分类路由模型（Self-Limited / Global-Complex / Always-False）。
- **暂不落地**：Context 路由器具体实现细节。

### 1.2 强约束

1. `Evaluator` 主流水线顺序不变。
2. Collector 不承担 Context 推理，只接收路由后的事实。
3. 动态假设单次更新上限不变（最多一次）。
4. 最终输出对象保持 Immutable。

---

## 2. 新问题定义：Collector 不再是“全路径统一标签容器”

在 Context 三分类下，结论来源天然分裂：

1. 第一类（Self-Limited）走语义流水线，产出结构化风险。
2. 第二类（Global-Complex）走原始直判，不经过语义风险阶段。
3. 第三类（Always-False）直接短路 SAFE。

因此，Collector 不能再假设“所有路径都要打同一套阶段标签”。

**重定义：** Collector 负责“来源化收集和结果装配”，而不是“统一堆叠标签”。

---

## 3. 结果来源模型（必须显式化）

建议把来源定义为一等字段：

- `SEMANTIC_PIPELINE`：第一类语义流水线结果。
- `CONTEXT_DIRECT`：第二类直判结果。
- `SHORT_CIRCUIT_SAFE`：第三类或其他短路安全结果。

每条输出至少包含：

- `origin`
- `severity`
- `explanation`

可选包含：

- `evidence`（例如原始 `conflicts()` 比较摘要）

---

## 4. Collector 职责重定义

## 4.1 职责边界

Collector 只做三件事：

1. 接收固定风险写入（主要来自 `SEMANTIC_PIPELINE`）。
2. 管理有限动态假设（一次更新）。
3. 生成最终不可变结果（按来源装配）。

Collector 不负责：

- 判断 Context 属于哪一类。
- 决定是否进入语义流水线。
- 复写或解释 `IKeyConflictContext` 行为。

## 4.2 来源化写入接口建议

建议面向来源提供明确入口：

- `recordSemantic(tag, severity)`
- `recordDirect(severity, explanation, evidence?)`
- `recordShortCircuitSafe(explanation)`

这样可以避免第二/第三类路径被迫“伪装成标签流水线结果”。

---

## 5. 固定流程下的高性能实现策略

## 5.1 内部结构

- 语义路径：字段槽位化 + 固定缓冲区。
- 动态假设：固定槽位数组，O(1) 访问，一次更新约束。
- 直判/短路路径：最小结构写入（不创建多余风险对象）。

## 5.2 明确禁止

- 运行时 `Class` 扫描查找风险。
- 基于 `instanceof` 的全表匹配。
- 为了统一格式而给直判/短路路径堆叠无意义标签。

---

## 6. 标签策略更新（Hardware/Context 裁剪）

在三来源模型中：

1. `SEMANTIC_PIPELINE` 保留必要标签与风险。
2. `CONTEXT_DIRECT` 默认不输出 Hardware/Context 阶段调试标签。
3. `SHORT_CIRCUIT_SAFE` 仅输出来源与说明，不输出冗余标签。

目标：
- 防止 SAFE 结果包含大量噪声标签。
- 把“可解释性”从标签堆叠迁移到 `explanation`。

---

## 7. UserOverride 简化后对结果结构的影响

UserOverride 采用“目标结论 + 说明文本”后：

- Collector 可直接产出来源为 override 的结论分支。
- 不再需要回放每个阶段标签覆盖逻辑。

建议最小字段：

- 目标键对标识
- 目标结果级别
- 用户说明文本
- 来源标识（override）

这样既保留可解释性，也保留最小审计能力。

---

## 8. ConflictResult 最小协议建议

建议 `ConflictResult` 对外收敛为最小协议，避免与 Collector 内部策略耦合：

- `origin`
- `severity`
- `explanation`
- `details`（可选：语义风险列表或 evidence）

若需要兼容旧消费方，可在适配层把 `details` 展平，不在核心模型里维持旧分层复杂结构。

---

## 9. 需要替换的旧叙述（文档级）

以下旧叙述应视为废弃：

1. “所有路径进入统一标签流水线”。
2. “Hardware/Context 标签默认总是有价值”。
3. “Collector 负责全链路推理和标签归一”。
4. “Override 需要逐层逐标签覆盖”。
5. “直判/短路路径也必须构造成完整语义风险列表”。

替换为：

- 按来源收集最小必要信息。
- 语义路径与直判/短路路径分流表达。
- 解释文本优先于冗余调试标签。

---

## 10. 迁移计划（Result 优先）

### Step 1：定义结果协议

先确定 `ConflictResult` 最小字段（origin/severity/explanation/evidence?）。

### Step 2：改 Collector 为来源化写入

- 保留语义路径槽位化实现。
- 新增 direct/short-circuit 快路径写入。

### Step 3：装配器改造

引入统一装配器，将三来源统一装配成 Immutable 结果。

### Step 4：裁剪标签策略

仅保留语义路径必要标签；直判/短路路径走说明文本。

### Step 5：接入 Context 新路由输入

Context 层改造落地时，仅替换 Collector 输入来源，不改 Result 对外契约。

---

## 11. 测试重点

## 11.1 单元测试

1. 槽位化假设一次更新约束。
2. `recordDirect` 与 `recordShortCircuitSafe` 不依赖语义标签。
3. 装配器三来源合并后的严重度规则。

## 11.2 回归测试

1. 第一类场景：语义路径结果与旧基线一致（允许文案差异）。
2. 第二类场景：确认不进入语义流水线。
3. 第三类场景：直接 SAFE 短路。
4. 标签裁剪后仍可定位结论来源。

## 11.3 验证指标

- `conflicts()` 调用次数变化（用于验证第二类直判路径收益）。
- 输出结构稳定性（origin 分布是否符合预期）。

---

## 12. 验收 Checklist

- [ ] 文档和设计均明确三来源结果模型。
- [ ] Collector 职责已重定义为“来源化收集 + 装配”。
- [ ] 第二类路径不再被强制塞入语义标签结构。
- [ ] 第三类路径输出为最小 SAFE 结果。
- [ ] Hardware/Context 冗余标签已裁剪并有替代说明字段。
- [ ] UserOverride 采用“结论 + 文本说明”的最小输入模型。
- [ ] `ConflictResult` 最小协议稳定，后续 Context 改造无需改动对外契约。
- [ ] 测试覆盖三来源分支与严重度聚合。

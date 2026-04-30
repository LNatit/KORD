# Evaluator.java 代码审查报告

审查日期：2026-04-28
审查对象：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

## 发现的问题列表

### 🔴 **严重问题**

#### 1. ContextCollector.collect() 方法中的逻辑缺陷 [第 84-86 行]
**位置**：`src/main/java/com/lnatit/chord/result/context/ContextCollector.java`

**问题**：
```java
for (ConflictRisk field : new ConflictRisk[]{state, intercept, redirect, resource, intent, modality}) {
    if (field != null) {risks.add(field);}
    else {break;}  // ❌ 这里会跳过后续的非null字段
}
```

**危害**：
- 如果 `state` 为 null 但 `intercept` 不为 null，`intercept` 将被跳过
- 导致结果收集不完整，丢失风险信息
- 违反了流水线设计中"所有阶段内容都会跟随收集本"的原则

**修复方案**：
```java
for (ConflictRisk field : new ConflictRisk[]{state, intercept, redirect, resource, intent, modality}) {
    if (field != null) {
        risks.add(field);
    }
    // 删除 else break，只跳过null值，继续检查后续字段
}
```

---

#### 2. Evaluator.conflicts() 方法中对非Semantical语义的处理不完善 [第 48-56 行]
**位置**：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

**问题**：
```java
// 这一部分处理了KeySemantic.Semantical的情况
if (leftSemantic instanceof KeySemantic.Semantical(HashMap<KeyContext, ContextSemantic> leftMap)
    && rightSemantic instanceof KeySemantic.Semantical(HashMap<KeyContext, ContextSemantic> rightMap)) {
    // ... 处理逻辑
}

// 但对RawContext的处理可能不够精确
var collector = Collector.custom();
for (var leftCtx : leftSemantic.getContexts()) {
    for (var rightCtx : rightSemantic.getContexts()) {
        if (isContextOverlapping(leftCtx, rightCtx)) {
            collector.add(new KeyContext.Pair(leftCtx, rightCtx), 
                RiskEntry.create(RiskTag.of("context_overlap"), Severity.SEVERE));
            // ❌ RiskTag.of() 在这里可能不存在
        }
    }
}
```

**危害**：
- `RiskTag.of()` 方法可能不存在，导致编译失败
- 处理流程不一致，对于 Semantical 和 RawContext 的处理方式差异过大

**修复方案**：
- 验证 RiskTag 的 API，如果没有 `of()` 方法，使用正确的方式创建标签
- 或为上下文重叠创建一个专用的 RiskTag

---

### 🟡 **中等问题**

#### 3. 流水线顺序与文档不一致 [第 84-91 行]
**位置**：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

**当前代码的执行顺序**：
```
1. evaluateStateMutex
2. evaluateIntercept
3. evaluateRedirect
4. evaluateResource
5. evaluateIntent
6. evaluateModality
```

**PIPELINE_zh-cn.md 规定的顺序**：
```
阶段4：状态互斥校验
阶段5：输入拦截规则校验
阶段6：重定向模式校验
阶段7：意图匹配校验 + 操作模态校验
阶段8：资源读写冲突校验
```

**问题分析**：
- resource 和 intent 的相对位置与文档不符（旧管道中资源在阶段8，但代码中在第4位）
- 这可能是有意的重构，但**必须更新文档**以反映实际实现

**建议**：
- 确认这个顺序变化是有意的还是遗漏的，并在代码注释中说明
- 更新 PIPELINE_zh-cn.md 反映新的阶段顺序

---

#### 4. evaluateStateMutex() 中的 STATE_INTERSECT_RISK 可能未定义 [第 101 行]
**位置**：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

**代码**：
```java
collector.setState(StateTag.STATE_INTERSECT_RISK);
```

**问题**：
- StateTag.java 中定义了 `STATE_INTERSECT` 枚举值，但我们需要确认 `STATE_INTERSECT_RISK` 常量是否存在
- StateTag.java 第 14 行有 `STATE_INTERSECT_RISK` 的定义吗？

**验证方式**：
属于 StateTag.java 的第 10-14 行：
```java
STATE_MUTEX,
STATE_INTERSECT,
STATE_SUBSET;

public static final RiskEntry<StateTag> STATE_MUTEX_RISK = RiskEntry.diagnostic(STATE_MUTEX);
public static final RiskEntry<StateTag> STATE_INTERSECT_RISK = RiskEntry.diagnostic(STATE_INTERSECT);
public static final RiskEntry<StateTag> STATE_SUBSET_RISK = RiskEntry.diagnostic(STATE_SUBSET);
```

**结论**：✅ 已验证存在，无问题

---

### 🟢 **设计问题**

#### 5. evaluateIntercept() 方法中对 StateSubset 的处理逻辑需要澄清 [第 121-132 行]
**位置**：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

**代码**：
```java
RiskEntry<StateTag> stateRisk = collector.state();
if (stateRisk instanceof StateTag.StateSubset(boolean leftIsSubset) && 
    (leftIsSubset == li || leftIsSubset == !ri)) {
    // ...
}
```

**问题分析**：
- 这个逻辑的含义是：当存在状态子集关系 AND （左集合是子集的方向与拦截方向相同）
- 条件 `leftIsSubset == li || leftIsSubset == !ri` 的含义不够直观
- 虽然看起来合理，但应该添加注释说明这个条件的业务含义

**建议**：
添加详细的内联注释，如：
```java
// 部分覆盖：一个集合是子集，且只有拦截方在较大集合中
// 这样一方总能拦截到，而另一方不一定能获得输入
if (stateRisk instanceof StateTag.StateSubset(boolean leftIsSubset) && 
    (leftIsSubset == li || leftIsSubset == !ri)) {
    // ...
}
```

---

#### 6. evaluateResource() 方法中缺乏代码注释 [第 147-187 行]
**位置**：`src/main/java/com/lnatit/chord/eval/Evaluator.java`

**问题**：
- 资源冲突检测包含多层嵌套的 if-else
- 对于 LCA（最近公共祖先）概念的处理没有注释
- `evaluateSameResource()` 和 `evaluateAncestorResource()` 的调用条件不够清晰

**建议**：
- 补充资源树层级关系的说明
- 标注 `Resource.getLCA()` 的返回值含义
- 在 `evaluateAncestorResource()` 调用前添加注释说明什么时候会触发

---

### 📝 **文档同步问题**

#### 7. 代码与文档的映射不明确
**相关文件**：
- `PIPELINE_zh-cn.md` - 描述了 8 个阶段
- `CONTRIBUTOR_GUIDE_zh-cn.md` - 列出了 7 个语义维度加 3 个分析层
- 实际代码实现了 6 个 evaluate 方法

**问题**：
- 文档宣称有 7 个维度 + 8 个流水线阶段，但代码实现可能有差异
- 需要明确代码中的 6 个 evaluate 方法如何映射到 8 个流水线阶段

**映射关系（需要验证）**：
```
阶段1：物理键 ← isSameKey()
阶段2：用户覆盖 ← 代码注释为 TODO
阶段3：场景重叠 ← conflicts() 中的 context 过滤
阶段4-5：状态+拦截 ← evaluateStateMutex() + evaluateIntercept()
阶段6：重定向 ← evaluateRedirect()
阶段7：意图+模态 ← evaluateIntent() + evaluateModality()
阶段8：资源 ← evaluateResource()
```

---

## 修复优先级总结

| 优先级 | 问题 | 影响范围 | 修复时间估计 |
|--------|------|--------|-----------|
| P0 🔴 | ContextCollector.collect() 的 break 逻辑 | 所有冲突检测结果 | 5 分钟 |
| P0 🔴 | conflicts() 中的 RiskTag.of() 问题 | RawContext 处理路径 | 15 分钟 |
| P1 🟡 | 流水线顺序与文档不符 | 文档更新 | 30 分钟 |
| P2 🟢 | evaluateIntercept() 逻辑注释 | 代码可维护性 | 10 分钟 |
| P2 🟢 | evaluateResource() 注释不足 | 代码可维护性 | 15 分钟 |
| P3 | 文档与代码映射不清 | 贡献者入门 | 45 分钟 |

---

## 推荐后续步骤

1. **立即修复** P0 问题（预计 10-20 分钟）
2. **验证** 流水线顺序的有意性，更新或确认 PIPELINE_zh-cn.md
3. **补充** 代码注释，提高可维护性
4. **更新** AGENTS.md 和文档以反映实现细节
5. **添加** 单元测试覆盖边界情况（特别是 null collect、RawContext 处理）


## 最终优化后的判定管线 (The Optimized Pipeline)

这是综合了上述分析后，最推荐你编写代码的结构顺序：

### Phase 1: 绝对物理隔离 (短路返回 SAFE)
1. **硬件层** (`keyCode` / `modifier` 不同 → SAFE)
2. **引擎上下文层** (`IN_GAME` vs `GUI` 互斥 → SAFE)

### Phase 2: 最高权限与绝对逻辑隔离 (短路返回)
3. **玩家自定义层** (命中白名单 → SAFE，黑名单 → SEVERE)
4. **状态互斥层** (同一 `locomotion` 但取值不同 → SAFE)

### Phase 3: 拦截恶劣行为 (阻断降级机制)
5. **消息消费判定拦截** (若一方 `capture=consume`，打上标记 `isConsumed = true`，**不允许**在下一步被 Intent 轻易放行)

### Phase 4: 主观意图放行 (短路返回 SAFE / WARNING)
6. **意图合并层** (若 `intent` 相同，且 `isConsumed == false`)
    - 根据 Root 决定：低风险 Root → 返回 SAFE；高风险 Root → 返回 WARNING。

### Phase 5: 综合定级查表 (计算最终结果)
如果走到这里，说明：同一键位、可能同时发生、意图不同、没有被玩家豁免。
执行并行查表：
7. **动作冲突矩阵层** (查表得出基础破坏力，如 `combat` vs `gui_open` = SEVERE)
8. **操作模态层** (Hold vs Toggle = WARNING/SEVERE)
9. **交互提权层** (`interactive=true` 与 `act` 碰撞 = WARNING/SEVERE)
10. **消费提权** (如果有 `isConsumed` 标记，强行提至 SEVERE)
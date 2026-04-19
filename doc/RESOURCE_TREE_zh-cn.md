# Resource 层级语义设计

## 1. 设计目标

`Resource` 用于描述按键操作作用到的对象域，以树状层级而非扁平字符串来组织（如 `armors` -> `armors/helmet`）。

目标：

1. 精确描述操作对象的粒度（父域与子域可区分）。
2. 由字符串路径驱动，便于外部数据配置。
3. 用少量规则支持可解释的冲突分级。

---

## 2. 两层定义框架

Resource 系统分两个层次定义，彼此独立：

**第一层：资源类型定义（先行定义）**

定义资源树的结构与每个节点的并发写属性，与按键语义无关：

- 每个节点的路径（`/` 分隔，如 `armors/helmet`）
- 每个节点是否支持并发写（`supportsConcurrentWrites`）：
  - `true`：允许同时写入（如独立槽位）
  - `false`：不允许同时写入（操作间互斥）

所有节点的并发写属性均**显式声明**，不依赖推断或继承。

**第二层：按键语义定义**

每个按键只声明两件事：

- 操作了哪个资源（引用第一层中已定义的节点路径）
- 操作方式是**读**还是**写**

---

## 3. 节点类型约定

建议将树中节点区分为三类（便于语义描述，不强制约束实现）：

1. **聚合域（Aggregate）**：有子节点，`supportsConcurrentWrites=true`。子节点各自独立，允许同时操作不同子域。
2. **次原子域（Sub-Atomic）**：有子节点，`supportsConcurrentWrites=false`。该域作为整体不允许并发操作，但内部可进一步细分。
3. **原子域（Atomic Leaf）**：叶子节点，`supportsConcurrentWrites=false`。最小粒度，不可再分。

---

## 4. 冲突判定规则（资源维度）

设两条按键操作的资源路径分别为 A、B（已从树中定位到具体节点）。

### 规则一：相同节点

两个按键操作同一节点：

- 直接使用该节点的 `supportsConcurrentWrites` 判定。
- `true` → 不冲突；`false` → 冲突。

### 规则二：父子关系（祖先-后代）

一个按键操作父域，另一个操作其子域（如 `armors` vs `armors/helmet`）：

- 使用**父节点**的 `supportsConcurrentWrites` 判定。
- 原因：父操作的范围覆盖子域，父节点的并发策略决定这类跨粒度操作是否互斥。
- `armors=false` → 冲突；`armors=true` → 不冲突。

### 规则三：兄弟关系

两个按键操作同一父节点下的不同子节点（如 `armors/helmet` vs `armors/chestplate`）：

- 直接判定为**不冲突（或低风险）**。
- 原因：两者操作的是同一父域下彼此独立的子域，物理上不会产生写冲突。
- 父节点的 `supportsConcurrentWrites` 在此不参与判定。

---

## 5. 示例

### 5.1 相同节点

- `keyA` 写 `armors`，`keyB` 写 `armors`。
- `armors=false` → CONCURRENT_WRITE（冲突）。

### 5.2 父子关系

- `keyA` 写 `armors`（整套替换），`keyB` 写 `armors/helmet`（仅头盔）。
- 取父节点 `armors`，`armors=false` → 冲突。
- 即使 `helmet=true`，因父操作覆盖整个域，仍判为冲突。

### 5.3 兄弟关系

- `keyC` 写 `armors/helmet`，`keyD` 写 `armors/chestplate`。
- 两者是兄弟节点，彼此独立 → 不冲突。
- `armors` 的并发写属性在此不参与判定。

---

## 6. 与 Chord 流水线的对齐

1. `Resource.overlaps(...)` 可扩展为返回关系类型（SAME / ANCESTOR_DESCENDANT / SIBLING / DISJOINT），便于 Evaluator 按规则分支判定。
2. `Evaluator.evaluateResource(...)` 的严重度分级沿用：
   - 写-写：`CONCURRENT_WRITE`
   - 读-写：`READ_WRITE`
   - 读-读：`CONCURRENT_ACCESS`
   - 不冲突（兄弟/不相交）：`RESOURCE_MUTEX`（无风险）
3. 冲突最终等级仍由整条 pipeline（含拦截、意图、模态）共同修正。

---

## 7. 工程约束

- 路径规范化：统一小写、去除首尾 `/`、禁止空段。
- 所有节点并发写属性均需显式声明，不依赖隐式继承。
- 资源注册（第一层）必须先于按键语义加载（第二层）完成。

---

## 8. 最简共识

- `Resource` 以树结构组织，两层定义彼此独立。
- 相同节点：看自身属性。
- 父子关系：看父节点属性（父操作覆盖子域）。
- 兄弟关系：直接不冲突（各自为独立原子域）。

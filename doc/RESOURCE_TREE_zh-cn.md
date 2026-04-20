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
- 若两者均为写：
  - `true` → 记为 `CONCURRENT_WRITE`（调试级，表示发生了并发写，但该资源允许这种写入）
  - `false` → 记为 `CONCURRENT_MODIFICATION`（真正有害的并发修改）
- 若为读写混合：记为 `READ_WRITE`，严重度由 `supportsConcurrentWrites` 与拦截共同修正。
- 若两者均为只读：记为 `CONCURRENT_ACCESS`（调试级）。

### 规则二：父子关系（祖先-后代）

一个按键操作父域，另一个操作其子域（如 `armors` vs `armors/helmet`）：

父子冲突的实质是两个操作都会最终触及子资源，因此**子节点的 `supportsConcurrentWrites` 是风险的核心依据，父节点在此基础上做一级调整**：

| 父节点 | 子节点 | 风险等级 | 语义解释 |
|--------|--------|----------|----------|
| `false` | `false` | SEVERE | 子资源不可并发（核心冲突），父域也禁止并发（双重互斥） |
| `true`  | `false` | WARNING | 子资源不可并发（核心冲突），但父域设计允许并发，略微缓解 |
| `false` | `true`  | INFO    | 子资源允许并发（核心安全），但父域有整体保护意图，保留低风险提示 |
| `true`  | `true`  | SAFE    | 子资源允许并发，父域允许并发；写写场景下仅记录 `CONCURRENT_WRITE` 调试信息 |

### 规则三：兄弟关系

两个按键操作同一父节点下的不同子节点（如 `armors/helmet` vs `armors/chestplate`）：

- 直接判定为 `RESOURCE_MUTEX`（调试级，无冲突）。
- 原因：两者操作的是同一父域下彼此独立的子域，物理上不会产生写冲突。
- 父节点的 `supportsConcurrentWrites` 在此不参与判定。

---

## 5. 示例

### 5.1 相同节点

- `keyA` 写 `armors`，`keyB` 写 `armors`。
- `armors=false` → `CONCURRENT_MODIFICATION`，严重度为 `SEVERE`。
- 若 `armors=true` → `CONCURRENT_WRITE`（调试级，表示该并发写是允许的）。

### 5.2 父子关系（父false + 子false）

- `keyA` 写 `armors`（整套替换，`armors=false`），`keyB` 写 `armors/helmet`（`helmet=false`）。
- 子节点不可并发 + 父节点不可并发 → SEVERE。

### 5.3 父子关系（父true + 子false）

- `keyA` 写 `inventory`（`inventory=true`，聚合域），`keyB` 写 `inventory/helmet`（`helmet=false`）。
- 子节点不可并发（核心冲突），但父域允许并发 → WARNING。

### 5.4 父子关系（父false + 子true）

- `keyA` 写 `armors`（`armors=false`），`keyB` 写 `armors/hotbar`（`hotbar=true`）。
- 子节点允许并发（核心安全），但父域有整体保护意图 → INFO。

### 5.5 兄弟关系

- `keyC` 写 `armors/helmet`，`keyD` 写 `armors/chestplate`。
- 两者是兄弟节点，彼此独立 → `RESOURCE_MUTEX`。
- 父节点的并发写属性在此不参与判定。

---

## 6. 与 Chord 流水线的对齐

1. 当前实现通过 `sRes == oRes` 与 `Resource.getLCA(...)` 区分 SAME / ANCESTOR_DESCENDANT / 其他关系；兄弟与不相交统一落为 `RESOURCE_MUTEX`。
2. `Evaluator.evaluateResource(...)` 的严重度分级沿用：
   - 有害写-写：`CONCURRENT_MODIFICATION`
   - 无害并发写：`CONCURRENT_WRITE`（调试级）
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
- 父子关系：子节点属性决定风险核心（子节点优先），父节点属性做一级调整。
- 兄弟关系：记为 `RESOURCE_MUTEX`（各自为独立原子域）。

## 9. 资源冲突判定矩阵（Evaluator 层）

### 9.1 判定涉及的三个维度

资源冲突的最终判定由三个维度共同决定：

1. **关系类型**（当前由 `==` 与 `Resource.getLCA(...)` 决定）
   - SAME：操作相同资源
   - ANCESTOR_DESCENDANT：父域操作与子域操作
   - SIBLING：操作同一父域下的不同子域
   - DISJOINT：资源无重叠

2. **读写类型**（Semantic.readOnly 决定）
   - RR：读 ∧ 读
   - WW：写 ∧ 写
   - RW：读 ⊕ 写

3. **Resource 并发属性**
   - `child_support=true`：子资源允许并发写
   - `child_support=false`：子资源不允许并发写
   - `parent_support=true`：父资源允许并发写（仅父子关系适用）
   - `parent_support=false`：父资源不允许并发写（仅父子关系适用）

### 9.2 判定规则表

#### 关系：DISJOINT（无重叠）
无论读写如何：
- 结果：`RESOURCE_MUTEX`（调试级，无冲突）

#### 关系：SAME（相同资源）

| 读写 | supportsConcurrentWrites | 结果 | 等级 |
|------|----------------------|--------|------|
| RR | 任意 | CONCURRENT_ACCESS | 调试级 |
| WW | true | CONCURRENT_WRITE | 调试级 |
| WW | false | CONCURRENT_MODIFICATION | SEVERE（若有拦截 INFO） |
| RW | 任意 | READ_WRITE | INFO/WARNING（见下） |

RW 的读写混合规则：
- `supportsConcurrentWrites = true` 或有拦截 → INFO
- `supportsConcurrentWrites = false` 且无拦截 → WARNING

#### 关系：ANCESTOR_DESCENDANT（父子关系）

**第一步**：确定谁是父、谁是子。

**第二步**：取**子资源的属性**作为主判定依据。

**第三步**：根据父资源属性**做一级调整**。

| 读写 | 子.support | 父.support | 结果 | 等级 | 说明 |
|------|----------|----------|--------|------|------|
| RR | 任意 | 任意 | CONCURRENT_ACCESS | 调试级 | 读-读无冲突 |
| WW | false | false | CONCURRENT_MODIFICATION | SEVERE | 子资源禁止并发，父域也禁止 → 双重互斥 |
| WW | false | true | CONCURRENT_MODIFICATION | WARNING | 子资源禁止并发（核心冲突），但父域允许 → 略微缓解 |
| WW | true | false | CONCURRENT_MODIFICATION | INFO | 子资源允许并发（核心安全），但父域禁止 → 低风险提示 |
| WW | true | true | CONCURRENT_WRITE | 调试级 | 都允许并发，但发生了无害并发写 |
| RW | false | 任意 | READ_WRITE | WARNING | 子资源禁止并发 → 读写混合有风险 |
| RW | true | 任意 | READ_WRITE | INFO | 子资源允许并发 → 低风险 |

拦截修正：当前实现会将资源维度上的有害冲突统一下调到 `INFO`（包括同节点 WW、同节点 RW、父子 WW、父子 RW 等分支）。

#### 关系：SIBLING（兄弟关系）

兄弟节点操作同一父域下的不同子域，物理上互不干扰：
- 结果：`RESOURCE_MUTEX`（调试级，无冲突）

---

### 9.3 拦截逻辑的作用

拦截（Interceptive）是一种对资源冲突的**修正因子**。

在当前实现中：

- 同节点写写冲突：`SEVERE -> INFO`
- 同节点读写冲突：`WARNING -> INFO`
- 父子写写冲突：`SEVERE/WARNING/INFO -> INFO`
- 父子读写冲突：`WARNING -> INFO`

**逻辑**：拦截意味着其中一个操作能“阻断”另一个操作的执行，从而实际冲突概率降低，因此资源维度的冲突会被统一弱化。

---

### 9.4 与当前代码的对应关系

当前 `evaluateResource` 已实现以下分支：

- `sRes == oRes`：相同资源
- `Resource.getLCA(sRes, oRes)` 为其中一方：父子关系
- 其他情况：兄弟或不相交，统一记为 `RESOURCE_MUTEX`

当前实现的额外约定：

1. `Resource.ROOT` 被视为“无具体资源域”，不参与资源冲突判断。
2. `CONCURRENT_WRITE` 仅表示“发生了并发写”，并不自动代表风险。
3. `CONCURRENT_MODIFICATION` 才表示真正有害的写写冲突。

# Minecraft 26.1.x / NeoForge 21.1.x — Screen 开发指南

> 基于 `minecraft-patched-26.1.2.36-beta` 实际源码分析。

---

## 目录

1. [总体渲染架构](#1-总体渲染架构)
2. [Screen 生命周期](#2-screen-生命周期)
3. [背景渲染与层级（Stratum）](#3-背景渲染与层级stratum)
4. [extractRenderState 与 super 调用](#4-extractrenderstate-与-super-调用)
5. [Widget 注册](#5-widget-注册)
6. [GuiGraphicsExtractor 绘图 API](#6-guigraphicsextractor-绘图-api)
7. [自定义 Widget：AbstractWidget](#7-自定义-widgetabstractwidget)
8. [自定义按钮：AbstractButton / Button](#8-自定义按钮abstractbutton--button)
9. [鼠标与键盘事件](#9-鼠标与键盘事件)
10. [剪裁（Scissor）与滚动列表](#10-剪裁scissor与滚动列表)
11. [Tooltip](#11-tooltip)
12. [坐标变换（Pose）](#12-坐标变换pose)
13. [常见陷阱](#13-常见陷阱)
14. [最小示例](#14-最小示例)

---

## 1. 总体渲染架构

新版本采用"状态提取"模型，而非直接 GPU 绘制：

```
Minecraft 调用
  └─ screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, a)
        ├─ graphics.nextStratum()                    // 层级 0 → 1（背景层）
        ├─ extractBackground(graphics, ...)          // 背景：模糊 / 贴图 / 全景图
        ├─ NeoForge 事件: BackgroundRendered
        ├─ graphics.nextStratum()                    // 层级 1 → 2（内容层）
        ├─ extractRenderState(graphics, ...)         // ← 你主要重写的地方
        └─ graphics.extractDeferredElements(...)     // Tooltip、Preedit 等延迟元素
```

`GuiGraphicsExtractor` 不直接发出 GPU 命令，而是收集 `GuiRenderState`，统一批量提交。因此绘制顺序由**调用顺序**决定，同一层级内先调用的在下方。

---

## 2. Screen 生命周期

```java
public class MyScreen extends Screen {

    // 1. 构造函数 — minecraft / font 已由父类自动初始化
    public MyScreen() {
        super(Component.literal("我的界面"));
    }

    // 2. init() — 每次界面被初始化时调用（首次打开 or 窗口尺寸变化）
    //    在此注册 Widget，计算布局
    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("确定"), b -> onClose())
                .bounds(width / 2 - 50, height / 2, 100, 20)
                .build());
    }

    // 3. extractRenderState() — 每帧调用，填充渲染状态
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // 先画背景，再 super（渲染所有已注册 widget）
        graphics.fill(0, 0, width, height, 0xFF222222);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    // 4. tick() — 每游戏刻（20/s）调用，适合动画计数器等
    @Override
    public void tick() { }

    // 5. removed() — 界面关闭后调用，用于清理资源
    @Override
    public void removed() { }

    // 6. added() — 界面被推入层级栈时调用
    @Override
    public void added() { }
}
```

| 回调 | 触发时机 | 典型用途 |
|---|---|---|
| `init()` | 首次打开 / 窗口缩放 | 注册 widget、计算布局 |
| `extractRenderState()` | 每帧 | 绘制背景、面板、浮层 |
| `extractBackground()` | 每帧（在 `extractRenderState` 之前的独立层） | 自定义背景（见下节）|
| `tick()` | 每刻 | 动画、轮询 |
| `removed()` | 关闭时 | 清理 |
| `repositionElements()` | 窗口缩放时（替代重新 init） | 重新计算坐标（默认实现是 `rebuildWidgets()`）|

---

## 3. 背景渲染与层级（Stratum）

### 3.1 默认背景行为

`extractBackground()` 由框架**自动在独立层级**调用，逻辑如下：

```java
// Screen.java — 简化版
public void extractBackground(GuiGraphicsExtractor graphics, ...) {
    if (isInGameUi()) {
        extractTransparentBackground(graphics); // 深色渐变遮罩
    } else {
        if (minecraft.level == null)
            extractPanorama(graphics, a);       // 主菜单全景图
        extractBlurredBackground(graphics);     // 高斯模糊
        extractMenuBackground(graphics);        // 瓷砖贴图（menu_background.png）
    }
}
```

**关键问题：** 如果你的 Screen 在游戏内打开，且没有覆盖 `extractBackground()`，默认会在全屏绘制一层半透明的 `inworld_menu_background`（灰色瓷砖贴图），导致整个界面蒙上一层灰雾。

### 3.2 修复灰雾 / 自定义背景的三种方式

**方式 A — 声明为游戏内 UI（推荐，使用深色渐变）**
```java
@Override
public boolean isInGameUi() {
    return true; // 使用 extractTransparentBackground（深色半透明渐变）
}
```

**方式 B — 完全禁用默认背景（自己全权负责）**
```java
@Override
public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    // 空实现：不渲染任何背景
    // extractRenderState() 中自己 graphics.fill(...) 绘制背景
}
```

**方式 C — 仅禁用模糊/贴图，保留全景图**
```java
@Override
protected void extractBlurredBackground(GuiGraphicsExtractor graphics) { }

@Override
protected void extractMenuBackground(GuiGraphicsExtractor graphics) { }
```

### 3.3 手动控制层级（nextStratum）

`nextStratum()` 让后续绘制内容在视觉上覆盖前一层所有内容，常用于浮层/弹窗：

```java
@Override
public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    super.extractRenderState(graphics, mouseX, mouseY, a);   // 正常内容层

    if (showPopup) {
        graphics.nextStratum();                              // 新层级，绝对在最上方
        renderPopup(graphics);
    }
}
```

---

## 4. extractRenderState 与 super 调用

```java
@Override
public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

    // ① 在 super 之前绘制 → 位于所有 widget 之下（背景）
    graphics.fill(0, 0, width, height, 0xFF1A1A2E);

    // ② super 渲染所有注册的 Renderable（widget）
    super.extractRenderState(graphics, mouseX, mouseY, a);

    // ③ 在 super 之后绘制 → 位于所有 widget 之上（浮层/遮罩）
    if (showOverlay) renderOverlay(graphics);
}
```

**不要**在 `init()` 返回后修改 `renderables` 列表（除非通过 `addRenderableWidget` / `removeWidget`），否则会产生并发修改异常。

---

## 5. Widget 注册

| 方法 | 加入 renderables | 加入 children（事件） | 加入 narratables（无障碍）|
|---|:---:|:---:|:---:|
| `addRenderableWidget(T)` | ✅ | ✅ | ✅ |
| `addRenderableOnly(T)` | ✅ | ❌ | ❌ |
| `addWidget(T)` | ❌ | ✅ | ✅ |
| `removeWidget(listener)` | ✅（如实现）| ✅ | ✅ |
| `clearWidgets()` | ✅ | ✅ | ✅ |

泛型约束：
- `addRenderableWidget` 要求 `T extends GuiEventListener & Renderable & NarratableEntry`
- `addRenderableOnly` 要求 `T extends Renderable`
- `addWidget` 要求 `T extends GuiEventListener & NarratableEntry`

---

## 6. GuiGraphicsExtractor 绘图 API

### 6.1 几何图形

```java
// 填充矩形（ARGB 颜色，注意：A=0 为透明，需 A>0 才可见）
graphics.fill(x0, y0, x1, y1, 0xFF334455);

// 垂直渐变矩形
graphics.fillGradient(x0, y0, x1, y1, topColor, bottomColor);

// 1px 边框
graphics.outline(x, y, width, height, color);

// 水平 / 垂直线段
graphics.horizontalLine(x0, x1, y, color);
graphics.verticalLine(x, y0, y1, color);
```

### 6.2 文字

```java
// 左对齐
graphics.text(font, "Hello", x, y, 0xFFFFFFFF);
graphics.text(font, component, x, y, color);
graphics.text(font, formattedCharSequence, x, y, color, dropShadow);

// 居中（x 为中心点）
graphics.centeredText(font, "Hello", centerX, y, color);
graphics.centeredText(font, component, centerX, y, color);

// 自动换行（width 为最大行宽，行高固定 9px）
graphics.textWithWordWrap(font, formattedText, x, y, width, color);

// 带背景色的文字（尊重无障碍"背景不透明度"设置）
graphics.textWithBackdrop(font, component, x, y, textWidth, color);
```

> **颜色格式：** `0xAARRGGBB`，注意 alpha 为 0 时文字不可见。

### 6.3 贴图 / 精灵

```java
// 从 GUI Atlas 渲染精灵（自动处理 Nine-Slice / Tile / Stretch）
graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteIdentifier, x, y, width, height);

// 带颜色叠加（包括 alpha，如 ARGB.white(0.5f) 表示 50% 透明）
graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, x, y, width, height, color);

// 直接贴纹理（需要完整 UV 参数）
graphics.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, u, v, width, height, texW, texH);
```

### 6.4 物品

```java
graphics.item(itemStack, x, y);
graphics.itemDecorations(font, itemStack, x, y);   // 耐久条 + 数量
```

### 6.5 剪裁

详见 [第 10 节](#10-剪裁scissor与滚动列表)。

### 6.6 获取屏幕尺寸（替代 Screen.width/height）

```java
int w = graphics.guiWidth();
int h = graphics.guiHeight();
```

---

## 7. 自定义 Widget：AbstractWidget

```java
public class MyWidget extends AbstractWidget {

    public MyWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    // ▶ 必须实现：渲染逻辑
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics,
                                             int mouseX, int mouseY, float a) {
        // isHovered() 由框架在每帧自动更新，无需手动计算
        int bg = isHovered() ? 0x80FFFFFF : 0x40FFFFFF;
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
        graphics.centeredText(font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, 0xFFFFFFFF);

        // 可选：根据 active 状态切换光标样式
        handleCursor(graphics); // active → POINTING_HAND, inactive → NOT_ALLOWED
    }

    // ▶ 必须实现：无障碍旁白
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output); // 通用按钮旁白
    }

    // ▶ 可选：点击回调（左键触发）
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        // event.x(), event.y(), event.button()
    }

    // ▶ 可选：松开鼠标
    @Override
    public void onRelease(MouseButtonEvent event) { }

    // ▶ 可选：拖拽
    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) { }

    // ▶ 可选：接受右键等非左键点击
    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 0 || buttonInfo.button() == 1;
    }

    // ▶ 可选：滚轮（须重写，父类默认不消费）
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!isMouseOver(x, y)) return false;
        // scrollY > 0 向上
        return true;
    }
}
```

**AbstractWidget 常用字段与方法一览：**

| 成员 | 说明 |
|---|---|
| `active` | false 时禁用点击，`isMouseOver` 返回 false |
| `visible` | false 时完全不渲染、不接受事件 |
| `alpha` | 透明度 0～1，由框架传入 widget |
| `isHovered()` | 框架每帧更新，可直接使用 |
| `isFocused()` | 键盘焦点状态 |
| `isActive()` | `visible && active` |
| `getX/Y/Width/Height()` | 几何信息 |
| `setX/Y/Width/Height()` | 修改位置尺寸 |
| `setTooltip(Tooltip)` | 设置悬浮提示 |
| `setAlpha(float)` | 设置透明度（Supply chain：框架调用 `fadeWidgets`）|
| `setMessage(Component)` | 修改显示文本 |

---

## 8. 自定义按钮：AbstractButton / Button

继承链：`Button` → `AbstractButton` → `AbstractWidget.WithInactiveMessage` → `AbstractWidget`

### 8.1 使用 Button（最简）

```java
// Builder 模式（推荐）
Button btn = Button.builder(Component.literal("点我"), b -> doSomething())
        .bounds(x, y, 100, 20)
        .tooltip(Tooltip.create(Component.literal("提示")))
        .build();
addRenderableWidget(btn);

// 直接构造（需在子类中使用）
// super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
```

### 8.2 自定义外观：继承 Button，重写 extractContents

```java
private class FlatButton extends Button {
    FlatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // 不调用 extractDefaultSprite()，完全自定义：
        if (isHovered()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x60FFFFFF);
        }
        graphics.centeredText(font, getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, 0xFFFFFFFF);
        // handleCursor 已由 AbstractButton.extractWidgetRenderState() 自动调用
    }
}
```

> **注意：** `AbstractButton.extractWidgetRenderState()` 是 `final` 的，它调用 `extractContents()` + `handleCursor()`；**不要重写** `extractWidgetRenderState`，只重写 `extractContents`。

### 8.3 接受回车/空格触发

`AbstractButton.onPress(InputWithModifiers)` 同时被鼠标点击（`onClick`）和键盘选中（`keyPressed`）触发，因此只需实现 `onPress` 即可兼容键盘操作。`Button.onPress` 默认转调 `this.onPress.onPress(this)` lambda。

### 8.4 接受右键

```java
@Override
protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
    return buttonInfo.button() == 0 || buttonInfo.button() == 1;
}

@Override
public void onClick(MouseButtonEvent event, boolean doubleClick) {
    if (event.button() == 0) onLeftClick();
    else if (event.button() == 1) onRightClick();
}
```

---

## 9. 鼠标与键盘事件

### 9.1 Screen 级事件重写

```java
// 鼠标点击（在 super 之前拦截可阻止 widget 处理）
@Override
public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    // event.x(), event.y(), event.button()  (0=左, 1=右, 2=中)
    if (interceptCondition) {
        doSomething();
        return true;   // true = 消费事件，widget 不再收到
    }
    return super.mouseClicked(event, doubleClick);
}

// 鼠标松开
@Override
public boolean mouseReleased(MouseButtonEvent event) {
    return super.mouseReleased(event);
}

// 鼠标拖拽
@Override
public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
    return super.mouseDragged(event, dx, dy);
}

// 滚轮
@Override
public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
    return super.mouseScrolled(x, y, scrollX, scrollY);
}

// 键盘按下
@Override
public boolean keyPressed(KeyEvent event) {
    // event.key()（keyCode）, event.isEscape(), event.isSelection()（回车/空格）
    // event.hasShiftDown(), event.hasControlDown(), event.hasAltDown()
    return super.keyPressed(event);
}

// 字符输入（可输入框用）
@Override
public boolean charTyped(char c, int modifiers) {
    return super.charTyped(c, modifiers);
}
```

### 9.2 事件分发流程

```
Screen.mouseClicked(event, doubleClick)
  └─ AbstractContainerEventHandler.mouseClicked()
       └─ 遍历 children，找第一个 isMouseOver + 返回 true 的 widget
```

**事件消费语义：** 返回 `true` 表示已消费，框架不会继续向其他 widget 分发。

---

## 10. 剪裁（Scissor）与滚动列表

```java
// 启用剪裁（超出范围的绘制内容不可见）
graphics.enableScissor(x0, y0, x1, y1);

// 绘制内容...

// 必须配对调用
graphics.disableScissor();
```

剪裁是**栈结构**，支持嵌套；内层矩形会与外层取交集。

**典型滚动列表模式：**

```java
private class ScrollableList extends AbstractWidget {
    private int scrollOffset;
    private static final int ROW_H = 20;

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
        g.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());

        int y0 = getY() - scrollOffset;
        for (int i = 0; i < items.size(); i++) {
            int rowY = y0 + i * ROW_H;
            // 跳过完全不可见的行
            if (rowY + ROW_H < getY() || rowY > getY() + getHeight()) continue;
            renderRow(g, items.get(i), rowY);
        }

        g.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!isMouseOver(x, y)) return false;
        int maxScroll = Math.max(0, items.size() * ROW_H - getHeight());
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * ROW_H));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }
}
```

---

## 11. Tooltip

### 11.1 Widget 级 Tooltip（推荐）

```java
widget.setTooltip(Tooltip.create(Component.literal("提示文字")));
widget.setTooltipDelay(Duration.ofMillis(500)); // 延迟显示
```

框架会在 hover 时自动处理显示时机与位置，无需手动绘制。

### 11.2 手动设置 Tooltip（Screen 内）

```java
// 在 extractRenderState() 中调用，将在当帧末尾渲染
graphics.setTooltipForNextFrame(font, Component.literal("提示"), mouseX, mouseY);

// 多行
graphics.setComponentTooltipForNextFrame(font, List.of(line1, line2), mouseX, mouseY);

// 物品 Tooltip（自动包含物品信息）
graphics.setTooltipForNextFrame(font, itemStack, mouseX, mouseY);
```

---

## 12. 坐标变换（Pose）

`graphics.pose()` 返回一个 `Matrix3x2fStack`，支持平移、缩放、旋转：

```java
graphics.pose().pushMatrix();
graphics.pose().translate(offsetX, offsetY);
graphics.pose().scale(2.0f, 2.0f);
// 在变换空间内绘制
graphics.fill(0, 0, 50, 50, 0xFFFF0000);
graphics.pose().popMatrix();  // 还原变换
```

> **注意：** `pushMatrix` / `popMatrix` 必须配对，否则会影响后续所有绘制。

---

## 13. 常见陷阱

### ① 整个画面覆盖一层灰雾

**原因：** 父类 `extractBackground()` 默认在游戏内绘制 `inworld_menu_background`（半透明灰色瓷砖贴图）。

**修复：**
```java
// 方案 A：声明为游戏内 UI（改为深色渐变遮罩）
@Override
public boolean isInGameUi() { return true; }

// 方案 B：完全自定义背景
@Override
public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    // 留空，在 extractRenderState() 中自行绘制背景
}
```

### ② 颜色不透明（alpha=0）

ARGB 中 `0x00RRGGBB` 的 alpha 为 0，完全透明。固体颜色须 `0xFF??????`。

### ③ Widget 在 init() 之外注册

`width` / `height` 在 `init(int, int)` 调用后才有正确值；构造函数中不可使用。

### ④ 忘记调用 super.extractRenderState()

`Screen.extractRenderState()` 负责遍历并渲染所有注册的 widget。如果不调用，所有 widget 都不会显示。

### ⑤ fill 坐标顺序错误

`fill(x0, y0, x1, y1, color)` — 内部会自动交换确保 x0 < x1（源码已处理），但 y 轴**不会**自动交换；`y0` 应 < `y1`。

### ⑥ AbstractButton.extractWidgetRenderState 为 final

不要重写它，只重写 `extractContents()`。

### ⑦ mouseClicked 中 double 强转 int 精度

```java
int idx = (int)(event.y() - getY() + scrollOffset) / ROW_H;
// 应写为：
int idx = (int)((event.y() - getY() + scrollOffset) / ROW_H);
// 或先转 int 再除：
int relY = (int)(event.y() - getY()) + scrollOffset;
int idx = relY / ROW_H;
```

---

## 14. 最小示例

```java
public class DemoScreen extends Screen {

    public DemoScreen() {
        super(Component.literal("演示"));
    }

    // 游戏内打开时去除默认灰色背景
    @Override
    public boolean isInGameUi() { return true; }

    @Override
    protected void init() {
        addRenderableWidget(
            Button.builder(Component.literal("关闭"), b -> onClose())
                  .bounds(width / 2 - 50, height - 30, 100, 20)
                  .build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // ① 绘制面板背景（在 widget 之下）
        int panelX = width / 4, panelY = height / 4;
        int panelW = width / 2, panelH = height / 2;
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xC0202040);
        graphics.outline(panelX, panelY, panelW, panelH, 0xFF8888FF);
        graphics.centeredText(font, Component.literal("标题"),
                width / 2, panelY + 8, 0xFFFFFFFF);

        // ② 渲染所有已注册的 widget（按钮等）
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
```

---

*文档生成于 2026-05-06，基于 minecraft-patched-26.1.2.36-beta 及 neoforge-21.1.222 源码。*


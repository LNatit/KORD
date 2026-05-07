package com.lnatit.kord.gui;

import com.lnatit.kord.gui.front.KeyBindingSelectionListWidget;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class KeyBindingScreen extends Screen {
    private static final int COL_SIDEBAR_BG = 0xFF404040;
    private static final int COL_PANEL_BG = 0x80AAAAAA;
    private static final int COL_HOVER = 0x60404040;
    private static final int COL_SELECTED = 0x803366FF;
    private static final int COL_DETAIL_BG = 0x60888888;
    private static final int COL_TOGGLE_ACTIVE = 0x80334499;
    private static final int COL_MENU_BG = 0xFF333333;
    private static final int COL_MENU_BORDER = 0xFF888888;
    private static final int COL_MENU_HOVER = 0x80555555;
    private static final int COL_SEVERE = 0x80FF2222;
    private static final int COL_WARNING = 0x80FFAA00;
    private static final int COL_INFO = 0x80FFFF44;
    private static final int COL_SCROLLBAR_TRACK = 0x40FFFFFF;
    private static final int COL_SCROLLBAR_THUMB = 0xA0AAAAAA;

    private static final int PAD = 4;
    private static final int ROW_H = 20;
    private static final int SCROLLBAR_W = 4;
    private static final int SIDEBAR_BTN = 24;
    private static final int SIDEBAR_GAP = 2;
    private static final int ACTION_BTN_H = 26;
    private static final int FILTER_H = 20;
    private static final int MIN_W = 320;
    private static final int MIN_H = 240;
    private static final int TOOL_BUTTON_COUNT = 3;

    // Column layout — computed once in computeLayout()
    private int leftW;
    private int centerX;
    private int centerW;
    private int rightX;
    private int rightW;

    // Right-panel section layout — computed once in computeLayout()
    private int rightContentY;   // top of conflict list
    private int conflictListH;
    private int detailY;
    private int detailH;
    private int actionY;

    private List<MockConflict> conflicts = List.of();

    private int selectedConflictIndex = -1;
    private final boolean[] filterActive = {true, true, true};

    private int excludeMenuTarget = -1;
    private int excludeMenuX;
    private int excludeMenuY;

    /**
     * Kept so filter toggles can clamp its scroll offset.
     */
    private ConflictListWidget conflictListWidget;
    private @Nullable KeyBindingSelectionListWidget keyBindingListWidget;

    public KeyBindingScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.conflicts = getMockConflicts();
        this.excludeMenuTarget = -1;

        if (isTooSmall()) {
            return;
        }

        computeLayout();

        int sidebarCenterX = leftW / 2;

        FlatButton exit = new FlatButton(
                sidebarCenterX - SIDEBAR_BTN / 2, 2,
                SIDEBAR_BTN, SIDEBAR_BTN,
                Component.literal("✕"),
                b -> onExitClick()
        );
        exit.setTooltip(Tooltip.create(Component.literal("退出")));
        addRenderableWidget(exit);

        for (int i = 0; i < TOOL_BUTTON_COUNT; i++) {
            int btnY = height - (i + 1) * (SIDEBAR_BTN + SIDEBAR_GAP);
            int idx = i;
            FlatButton tool = new FlatButton(
                    sidebarCenterX - SIDEBAR_BTN / 2, btnY,
                    SIDEBAR_BTN, SIDEBAR_BTN,
                    Component.literal(toolButtonIcon(i)),
                    b -> onToolButtonClick(idx)
            );
            String tip = switch (i) {
                case 0 -> "工具0";
                case 1 -> "工具1";
                default -> "工具" + i;
            };
            tool.setTooltip(Tooltip.create(Component.literal(tip)));
            addRenderableWidget(tool);
        }

        this.keyBindingListWidget = new KeyBindingSelectionListWidget(
                minecraft,
                font,
                centerX + PAD,
                PAD,
                centerW - PAD * 2,
                height - PAD * 2,
                this::onKeySelected,
                this::onBindingLeftClick,
                this::onBindingRightClick
        );
        addRenderableWidget(this.keyBindingListWidget);

        int toggleW = (rightW - PAD * 2) / 3;
        String[] filterLabels = {"严重", "警告", "轻微"};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            addRenderableWidget(new ToggleButton(
                    rightX + PAD + i * toggleW, PAD,
                    toggleW - (i < 2 ? 1 : 0), FILTER_H,
                    Component.literal(filterLabels[i]),
                    true,
                    active -> {
                        filterActive[idx] = active;
                        if (conflictListWidget != null) {
                            conflictListWidget.clampScroll();
                        }
                        onFilterToggle(idx, active);
                    }
            ));
        }

        conflictListWidget = new ConflictListWidget(
                rightX + PAD, rightContentY,
                rightW - PAD * 2, conflictListH);
        addRenderableWidget(conflictListWidget);

        int actionBtnW = (rightW - PAD * 2 - 2) / 2;

        addRenderableWidget(new FlatButton(
                rightX + PAD, actionY,
                actionBtnW, ACTION_BTN_H,
                Component.literal("定位"),
                b -> {
                }
        ) {
            @Override
            protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
                return buttonInfo.button() == 0 || buttonInfo.button() == 1;
            }

            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {
                if (selectedConflictIndex < 0) return;
                if (event.button() == 0) onLocateLeftKey(selectedConflictIndex);
                else if (event.button() == 1) onLocateRightKey(selectedConflictIndex);
            }
        });

        addRenderableWidget(new FlatButton(
                rightX + PAD + actionBtnW + 2, actionY,
                actionBtnW, ACTION_BTN_H,
                Component.literal("排除"),
                b -> {
                }
        ) {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {
                if (selectedConflictIndex >= 0 && event.button() == 0) {
                    excludeMenuTarget = selectedConflictIndex;
                    excludeMenuX = getX();
                    excludeMenuY = getY() - 3 * ROW_H - 4;
                }
            }
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (isTooSmall()) {
            graphics.centeredText(font, Component.literal("分辨率不足"), width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        graphics.fill(0, 0, leftW, height, COL_SIDEBAR_BG);
        graphics.fill(centerX, 0, centerX + centerW, height, COL_PANEL_BG);
        graphics.fill(rightX, 0, rightX + rightW, height, COL_PANEL_BG);

        renderDetailSection(graphics);

        if (excludeMenuTarget >= 0) {
            renderExcludeMenu(graphics, mouseX, mouseY);
        }

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (excludeMenuTarget >= 0) {
            final int MENU_ITEM_COUNT = 3;
            int menuW = 120;
            int menuH = MENU_ITEM_COUNT * ROW_H + 4;
            int mx = Math.min(excludeMenuX, width - menuW - 2);
            int my = Math.min(excludeMenuY, height - menuH - 2);

            if (event.x() >= mx && event.x() < mx + menuW
                    && event.y() >= my && event.y() < my + menuH) {
                int itemIdx = (int) (event.y() - my - 2) / ROW_H;
                if (itemIdx >= 0 && itemIdx < MENU_ITEM_COUNT && event.button() == 0) {
                    onExcludeMenuAction(itemIdx);
                }
            }
            excludeMenuTarget = -1;
            this.clearSelectedKeyIfClickedElsewhere(event);
            return true;
        }

        this.clearSelectedKeyIfClickedElsewhere(event);
        return super.mouseClicked(event, doubleClick);
    }

    private void clearSelectedKeyIfClickedElsewhere(MouseButtonEvent event) {
        if (this.keyBindingListWidget != null
                && this.keyBindingListWidget.getSelectedIndex() >= 0
                && !this.keyBindingListWidget.isMouseOver(event.x(), event.y())) {
            this.keyBindingListWidget.clearSelection();
        }
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    private void computeLayout() {
        leftW = Math.min((int) (width * 0.10), 32);
        int rem = width - leftW;
        centerW = rem * 5 / 9;
        rightW = rem - centerW;
        centerX = leftW;
        rightX = leftW + centerW;

        // Right-panel vertical sections
        rightContentY = PAD + FILTER_H + PAD;
        int rightRemain = height - rightContentY - PAD;
        conflictListH = (int) (rightRemain * 0.60);
        detailY = rightContentY + conflictListH;
        detailH = (int) (rightRemain * 0.25);
        actionY = detailY + detailH + PAD;
    }

    private boolean isTooSmall() {
        return width < MIN_W || height < MIN_H;
    }

    // -------------------------------------------------------------------------
    // Rendering helpers
    // -------------------------------------------------------------------------

    private void renderDetailSection(GuiGraphicsExtractor graphics) {
        graphics.fill(rightX + PAD, detailY, rightX + rightW - PAD, detailY + detailH, COL_DETAIL_BG);

        if (selectedConflictIndex >= 0 && selectedConflictIndex < conflicts.size()) {
            MockConflict c = conflicts.get(selectedConflictIndex);
            graphics.textWithWordWrap(font, Component.literal(c.description()),
                    rightX + PAD + 2, detailY + 2,
                    rightW - PAD * 2 - 4, 0xFFDDDDDD);
        } else {
            graphics.text(font, Component.literal("(未选择冲突)"),
                    rightX + PAD + 2, detailY + 4, 0xFFAAAAAA);
        }
    }

    private void renderExcludeMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        String[] items = {"忽略此冲突", "自定义排除规则...", "从社区导入..."};
        int menuW = 120;
        int menuH = items.length * ROW_H + 4;
        int mx = Math.min(excludeMenuX, width - menuW - 2);
        int my = Math.min(excludeMenuY, height - menuH - 2);

        graphics.fill(mx - 1, my - 1, mx + menuW + 1, my + menuH + 1, COL_MENU_BORDER);
        graphics.fill(mx, my, mx + menuW, my + menuH, COL_MENU_BG);

        for (int i = 0; i < items.length; i++) {
            int iy = my + 2 + i * ROW_H;
            if (mouseX >= mx && mouseX < mx + menuW && mouseY >= iy && mouseY < iy + ROW_H) {
                graphics.fill(mx, iy, mx + menuW, iy + ROW_H, COL_MENU_HOVER);
            }
            graphics.text(font, items[i], mx + 4, iy + (ROW_H - 8) / 2, 0xFFEEEEEE);
        }
    }

    /**
     * Draws a minimal scrollbar (track + thumb) on the right edge of a widget.
     */
    private static void renderScrollbar(GuiGraphicsExtractor graphics,
                                        int wx, int wy, int ww, int wh,
                                        int scrollOffset, int totalRows) {
        int totalH = totalRows * ROW_H;
        if (totalH <= wh) return;   // nothing to scroll

        int trackX = wx + ww - SCROLLBAR_W;
        graphics.fill(trackX, wy, trackX + SCROLLBAR_W, wy + wh, COL_SCROLLBAR_TRACK);

        float ratio = (float) wh / totalH;
        int thumbH = Math.max(12, (int) (wh * ratio));
        int maxOffset = totalH - wh;
        int thumbY = wy + (int) ((wh - thumbH) * ((float) scrollOffset / maxOffset));
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, COL_SCROLLBAR_THUMB);
    }

    // -------------------------------------------------------------------------
    // Static helpers
    // -------------------------------------------------------------------------

    private static String toolButtonIcon(int index) {
        return switch (index) {
            case 0 -> "*";
            case 1 -> "R";
            case 2 -> "?";
            default -> "+";
        };
    }

    private static int severityTint(Severity severity) {
        return switch (severity) {
            case SEVERE -> COL_SEVERE;
            case WARNING -> COL_WARNING;
            case INFO -> COL_INFO;
            default -> 0;
        };
    }

    // -------------------------------------------------------------------------
    // Callbacks (override in subclasses)
    // -------------------------------------------------------------------------

    protected void onExitClick() {
    }

    protected void onToolButtonClick(int index) {
    }

    protected void onBindingLeftClick(int keyIndex) {
    }

    protected void onBindingRightClick(int keyIndex) {
    }

    protected void onKeySelected(int keyIndex) {
    }

    protected void onFilterToggle(int level, boolean active) {
    }

    protected void onConflictSelected(int conflictIndex) {
    }

    protected void onLocateLeftKey(int conflictIndex) {
    }

    protected void onLocateRightKey(int conflictIndex) {
    }

    protected void onExcludeMenuAction(int actionId) {
    }

    protected void onRequestTooltip(String text) {
    }

    // -------------------------------------------------------------------------
    // Mock data (expanded for scroll testing)
    // -------------------------------------------------------------------------

    protected List<MockConflict> getMockConflicts() {
        return List.of(
                new MockConflict("W", "UP", Severity.SEVERE,
                        "在游戏内，这两个按键均映射到前进操作，可能同时触发。"),
                new MockConflict("鼠标右键", "格挡", Severity.SEVERE,
                        "使用物品与格挡动作绑定了同一个键，可能导致行为相互覆盖。"),
                new MockConflict("鼠标右键", "E", Severity.SEVERE,
                        "在 GUI 界面中，鼠标右键与交互功能存在冲突。"),
                new MockConflict("[Mod] 传送", "H", Severity.SEVERE,
                        "Mod 传送功能与原版快捷键 H 重叠，可能在游戏内误触发传送。"),
                new MockConflict("[Mod] 无敌", "K", Severity.SEVERE,
                        "Mod 无敌切换与 K 键存在绑定重复，与部分 UI 热键冲突。"),
                new MockConflict("Shift", "Ctrl", Severity.WARNING,
                        "在菜单界面中，这两个修饰键可能同时触发。"),
                new MockConflict("Ctrl+Q", "Q", Severity.WARNING,
                        "快捷丢弃单个与丢弃全部在部分上下文中行为相近，容易误操作。"),
                new MockConflict("副手使用", "F", Severity.WARNING,
                        "F 键在游戏中同时用于副手互换与 FOV 调整候选键，存在轻度冲突。"),
                new MockConflict("Tab", "列出玩家", Severity.WARNING,
                        "Tab 键在输入框中用于切换，与显示玩家列表的绑定存在情景重叠。"),
                new MockConflict("[Mod] 飞行", "G", Severity.WARNING,
                        "Mod 飞行开关与 G 键存在冲突，在部分服务器地图中可能被误触。"),
                new MockConflict("Q", "Tab", Severity.INFO,
                        "轻微的上下文重叠；通常不会同时激活。"),
                new MockConflict("空格", "回车", Severity.INFO,
                        "在文本输入框外，这两个键可能都触发确认。"),
                new MockConflict("快捷栏6", "^", Severity.INFO,
                        "快捷栏 6 与部分键盘布局的 ^ 键 scancode 存在轻微重叠。"),
                new MockConflict("聊天", "T", Severity.INFO,
                        "聊天键与部分 Mod 的临时绑定冲突，通常不影响正常游戏。"),
                new MockConflict("平滑摄像机", "滚轮", Severity.INFO,
                        "鼠标滚轮同时绑定了平滑摄像机与快捷栏切换，在特定情境下可能互相干扰。"),
                new MockConflict("[Mod] 菜单", "M", Severity.INFO,
                        "Mod 菜单键与地图 Mod 的 M 键常见绑定位置重叠。"),
                new MockConflict("高级调试", "F3+Alt", Severity.INFO,
                        "调试组合键与部分系统快捷键（如 Windows Alt+F4 相邻）存在潜在误触。"),
                new MockConflict("地图放大", "=", Severity.INFO,
                        "地图缩放键与文本编辑器 = 键行为在全屏覆盖 GUI 中有微弱重叠。"),
                new MockConflict("截图", "F2", Severity.INFO,
                        "F2 截图键与部分外设宏软件默认绑定冲突，极少情况下误触发。"),
                new MockConflict("命令", "/", Severity.INFO,
                        "/ 键在某些输入法下会触发输入法候选框，与命令输入有轻微干扰。")
        );
    }

    public record MockConflict(String leftKey, String rightKey, Severity severity, String description) {
    }

    // -------------------------------------------------------------------------
    // Inner widgets
    // -------------------------------------------------------------------------

    private class FlatButton extends Button {
        FlatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            if (isHovered()) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COL_HOVER);
            }
            graphics.centeredText(font, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2, 0xFFFFFFFF);
        }
    }

    private class ToggleButton extends AbstractWidget {
        private boolean toggled;
        private final ToggleHandler handler;

        ToggleButton(int x, int y, int width, int height, Component message,
                     boolean initial, ToggleHandler handler) {
            super(x, y, width, height, message);
            this.toggled = initial;
            this.handler = handler;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            if (toggled) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COL_TOGGLE_ACTIVE);
            } else if (isHovered()) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COL_HOVER);
            }
            graphics.centeredText(font, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 9) / 2,
                    toggled ? 0xFFFFFFFF : 0xFFAAAAAA);
            handleCursor(graphics);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            toggled = !toggled;
            handler.onToggle(toggled);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }


    private class ConflictListWidget extends AbstractWidget {
        private int scrollOffset;

        ConflictListWidget(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        /**
         * Clamp scroll offset after filter changes to avoid blank space at the bottom.
         */
        void clampScroll() {
            int visibleCount = 0;
            for (MockConflict c : conflicts) {
                if (isVisible(c.severity())) visibleCount++;
            }
            int max = Math.max(0, visibleCount * ROW_H - getHeight());
            scrollOffset = Math.max(0, Math.min(max, scrollOffset));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());

            int usableW = getWidth() - SCROLLBAR_W;
            int visibleRow = 0;
            int totalVisible = 0;

            // First pass: count visible rows for scrollbar
            for (MockConflict c : conflicts) {
                if (isVisible(c.severity())) totalVisible++;
            }

            for (int i = 0; i < conflicts.size(); i++) {
                MockConflict c = conflicts.get(i);
                if (!isVisible(c.severity())) continue;

                int rowY = getY() - scrollOffset + visibleRow * ROW_H;
                visibleRow++;
                int rowBot = rowY + ROW_H;
                if (rowBot < getY() || rowY > getY() + getHeight()) continue;

                if (i == selectedConflictIndex) {
                    graphics.fill(getX(), rowY, getX() + usableW, rowBot, COL_SELECTED);
                }
                if (mouseX >= getX() && mouseX < getX() + usableW
                        && mouseY >= rowY && mouseY < rowBot) {
                    graphics.fill(getX(), rowY, getX() + usableW, rowBot, COL_HOVER);
                }

                int textCol = switch (c.severity()) {
                    case SEVERE -> 0xFFFF6666;
                    case WARNING -> 0xFFFFCC55;
                    case INFO -> 0xFFFFFF88;
                    default -> 0xFFCCCCCC;
                };
                graphics.text(font, c.leftKey() + " <-> " + c.rightKey(),
                        getX() + 4, rowY + (ROW_H - 9) / 2, textCol);
                graphics.fill(getX(), rowBot - 1, getX() + usableW, rowBot, 0x30FFFFFF);
            }

            graphics.disableScissor();
            renderScrollbar(graphics, getX(), getY(), getWidth(), getHeight(),
                    scrollOffset, totalVisible);
            handleCursor(graphics);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!isMouseOver(event.x(), event.y())) return false;

            int idxVisible = (int) (event.y() - getY() + scrollOffset) / ROW_H;
            if (idxVisible < 0) return false;

            int curVisible = 0;
            for (int i = 0; i < conflicts.size(); i++) {
                MockConflict c = conflicts.get(i);
                if (!isVisible(c.severity())) continue;
                if (curVisible == idxVisible) {
                    selectedConflictIndex = i;
                    onConflictSelected(i);
                    return true;
                }
                curVisible++;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
            if (!isMouseOver(x, y)) return false;
            int count = 0;
            for (MockConflict c : conflicts) {
                if (isVisible(c.severity())) count++;
            }
            int max = Math.max(0, count * ROW_H - getHeight());
            scrollOffset = (int) Math.max(0, Math.min(max, scrollOffset - scrollY * ROW_H));
            return true;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        private boolean isVisible(Severity severity) {
            return switch (severity) {
                case SEVERE -> filterActive[0];
                case WARNING -> filterActive[1];
                case INFO -> filterActive[2];
                default -> false;
            };
        }
    }

    @FunctionalInterface
    private interface ToggleHandler {
        void onToggle(boolean active);
    }
}

package com.lnatit.chord.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.lnatit.chord.result.risk.Severity;

import java.util.List;

public class KeyBindingScreen extends Screen
{
    private static final int MIN_SCREEN_WIDTH = 360;
    private static final int MIN_SCREEN_HEIGHT = 240;

    private static final int OUTER_PADDING = 10;
    private static final int INNER_PADDING = 6;
    private static final int ROW_HEIGHT = 18;
    private static final int TITLE_HEIGHT = 14;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int MIN_THUMB_HEIGHT = 14;
    private static final int PANEL_BG_TINT = 0x66D3D3D3;

    private final KeyBindingBackend data;
    private final KeyBindingScreenActions actions;

    private int leftX;
    private int leftY;
    private int leftW;
    private int leftH;

    private int rightX;
    private int rightY;
    private int rightW;
    private int rightH;

    private int rightTopX;
    private int rightTopY;
    private int rightTopW;
    private int rightTopH;

    private int rightBottomX;
    private int rightBottomY;
    private int rightBottomW;
    private int rightBottomH;

    private int leftSelectedIndex = -1;
    private int pairSelectedIndex = -1;

    private double leftScrollOffset = 0;
    private double pairScrollOffset = 0;

    private boolean leftScrollbarDragging = false;
    private boolean pairScrollbarDragging = false;
    private double leftDragGrabOffsetY = 0;
    private double pairDragGrabOffsetY = 0;

    private FocusPanel focusedPanel = FocusPanel.NONE;
    private SeverityFilter severityFilter = SeverityFilter.WARNING_AND_ABOVE;
    // null means right list is in global mode (not scoped to any left binding).
    private String pairScopeBindingId = null;

    private Button jumpToAButton;
    private Button jumpToBButton;
    private Button excludeButton;
    private Button promoteRuleButton;

    public KeyBindingScreen() {
        super(Component.literal("Chord Key Bindings"));
        this.data = KeyBindingBackend.global();
        // Operation callbacks are recreated for each screen instance.
        this.actions = KeyBindingScreenActions.noop();
    }

    @Override
    protected void init() {
        recalculateLayout();

        this.clearWidgets();
        if (!hasSufficientSpace()) {
            return;
        }

        int buttonY = rightBottomY + rightBottomH - 24;
        int buttonW = (rightBottomW - INNER_PADDING * 3) / 2;

        jumpToAButton = Button.builder(Component.literal("Jump to A"), b -> onActionJumpToA())
                              .bounds(rightBottomX, buttonY, buttonW, 20)
                              .build();
        jumpToBButton = Button.builder(Component.literal("Jump to B"), b -> onActionJumpToB())
                              .bounds(rightBottomX + buttonW + INNER_PADDING, buttonY, buttonW, 20)
                              .build();
        excludeButton = Button.builder(Component.literal("Exclude"), b -> onActionExclude())
                              .bounds(rightBottomX, buttonY - 24, buttonW, 20)
                              .build();
        promoteRuleButton = Button.builder(Component.literal("Promote Rule"), b -> onActionPromoteRule())
                                  .bounds(rightBottomX + buttonW + INNER_PADDING, buttonY - 24, buttonW, 20)
                                  .build();

        addRenderableWidget(jumpToAButton);
        addRenderableWidget(jumpToBButton);
        addRenderableWidget(excludeButton);
        addRenderableWidget(promoteRuleButton);

        if (!data.bindings().isEmpty()) {
            selectLeft(0);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderScreenBackdrop(guiGraphics);
        if (!hasSufficientSpace()) {
            renderInsufficientSpaceMessage(guiGraphics);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        // Let vanilla run first (including any blur/background pass), then draw our custom panels on top.
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderPanels(guiGraphics);
        renderLeftList(guiGraphics, mouseX, mouseY);
        renderRightTopList(guiGraphics, mouseX, mouseY);
        renderRightBottom(guiGraphics);

        // Re-render action buttons on top to keep them crisp above custom panels.
        renderActionButtons(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderActionButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (jumpToAButton != null) jumpToAButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (jumpToBButton != null) jumpToBButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (excludeButton != null) excludeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (promoteRuleButton != null) promoteRuleButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!hasSufficientSpace()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == 258) { // TAB
            focusedPanel = focusedPanel == FocusPanel.LEFT_LIST ? FocusPanel.RIGHT_TOP_LIST : FocusPanel.LEFT_LIST;
            return true;
        }

        if (focusedPanel == FocusPanel.LEFT_LIST) {
            if (moveSelection(data.bindings().size(), keyCode, true)) {
                return true;
            }
        }
        else if (focusedPanel == FocusPanel.RIGHT_TOP_LIST) {
            if (moveSelection(getVisiblePairs().size(), keyCode, false)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hasSufficientSpace()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            if (isInside(leftX, leftY, leftW, leftH, mouseX, mouseY)) {
                focusedPanel = FocusPanel.LEFT_LIST;
                if (tryStartScrollbarDrag(true, mouseX, mouseY)) {
                    return true;
                }
                if (selectLeftByMouse(mouseX, mouseY)) {
                    return true;
                }
                // Clicked blank area in left panel: clear right scoped view to global list.
                clearRightScopeToGlobal();
                return true;
            }

            if (isInside(rightTopX, rightTopY, rightTopW, rightTopH, mouseX, mouseY)) {
                focusedPanel = FocusPanel.RIGHT_TOP_LIST;
                if (handleFilterClick(mouseX, mouseY)) {
                    return true;
                }
                if (tryStartScrollbarDrag(false, mouseX, mouseY)) {
                    return true;
                }
                if (selectPairByMouse(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!hasSufficientSpace()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        if (button == 0) {
            if (leftScrollbarDragging) {
                updateScrollFromThumbDrag(true, mouseY);
                return true;
            }
            if (pairScrollbarDragging) {
                updateScrollFromThumbDrag(false, mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!hasSufficientSpace()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }

        leftScrollbarDragging = false;
        pairScrollbarDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!hasSufficientSpace()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        if (isInside(leftX, leftY, leftW, leftH, mouseX, mouseY)) {
            leftScrollOffset = clamp(leftScrollOffset - scrollY * ROW_HEIGHT, 0, maxLeftScroll());
            return true;
        }
        if (isInside(rightTopX, rightTopY, rightTopW, rightTopH, mouseX, mouseY)) {
            pairScrollOffset = clamp(pairScrollOffset - scrollY * ROW_HEIGHT, 0, maxPairScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!hasSufficientSpace()) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        recalculateLayout();
    }

    private void renderScreenBackdrop(GuiGraphics gg) {
        // Use a plain dim layer instead of vanilla renderBackground(), which applies blur in some contexts.
        gg.fill(0, 0, this.width, this.height, 0xCC101010);
    }

    private void renderInsufficientSpaceMessage(GuiGraphics gg) {
        Component title = Component.literal("Chord Key Bindings");
        Component line1 = Component.literal("Screen too small for this layout");
        Component line2 = Component.literal("Required: " + MIN_SCREEN_WIDTH + "x" + MIN_SCREEN_HEIGHT);
        Component line3 = Component.literal("Current: " + this.width + "x" + this.height);

        int boxW = 280;
        int boxH = 72;
        int x1 = (this.width - boxW) / 2;
        int y1 = (this.height - boxH) / 2;
        int x2 = x1 + boxW;
        int y2 = y1 + boxH;

        gg.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF606060);
        gg.fill(x1, y1, x2, y2, 0xCC1E1E1E);

        gg.drawCenteredString(this.font, title, this.width / 2, y1 + 8, 0xFFFFFF);
        gg.drawCenteredString(this.font, line1, this.width / 2, y1 + 24, 0xD8D8D8);
        gg.drawCenteredString(this.font, line2, this.width / 2, y1 + 38, 0xBEBEBE);
        gg.drawCenteredString(this.font, line3, this.width / 2, y1 + 52, 0xFFB85C);
    }

    private boolean hasSufficientSpace() {
        return this.width >= MIN_SCREEN_WIDTH && this.height >= MIN_SCREEN_HEIGHT;
    }

    private void renderPanels(GuiGraphics gg) {
        gg.fill(leftX - 1, leftY - 1, leftX + leftW + 1, leftY + leftH + 1, 0xFF505050);
        gg.fill(leftX, leftY, leftX + leftW, leftY + leftH, 0xFF202020);
        gg.fill(leftX, leftY, leftX + leftW, leftY + leftH, PANEL_BG_TINT);
        gg.drawString(font, Component.literal("Bindings"), leftX + INNER_PADDING, leftY + 2, 0xFFFFFF, false);

        gg.fill(rightX - 1, rightY - 1, rightX + rightW + 1, rightY + rightH + 1, 0xFF505050);
        gg.fill(rightX, rightY, rightX + rightW, rightY + rightH, 0xFF202020);
        gg.fill(rightX, rightY, rightX + rightW, rightY + rightH, PANEL_BG_TINT);

        gg.fill(rightTopX - 1, rightTopY - 1, rightTopX + rightTopW + 1, rightTopY + rightTopH + 1, 0xFF505050);
        gg.fill(rightTopX, rightTopY, rightTopX + rightTopW, rightTopY + rightTopH, 0xFF1E1E1E);
        gg.drawString(font, Component.literal("Conflicts by Pair"), rightTopX + INNER_PADDING, rightTopY + 2, 0xFFFFFF, false);

        gg.fill(rightBottomX - 1, rightBottomY - 1, rightBottomX + rightBottomW + 1, rightBottomY + rightBottomH + 1, 0xFF505050);
        gg.fill(rightBottomX, rightBottomY, rightBottomX + rightBottomW, rightBottomY + rightBottomH, 0xFF1E1E1E);
        gg.drawString(font, Component.literal("Details"), rightBottomX + INNER_PADDING, rightBottomY + 2, 0xFFFFFF, false);
    }

    private void renderLeftList(GuiGraphics gg, int mouseX, int mouseY) {
        int listTop = leftY + TITLE_HEIGHT;
        int listBottom = leftY + leftH - INNER_PADDING;
        int listH = listBottom - listTop;
        int innerW = leftW - SCROLLBAR_WIDTH - INNER_PADDING * 2;

        List<KeyBindingBackend.BindingEntry> entries = data.bindings();
        int start = (int) (leftScrollOffset / ROW_HEIGHT);
        int end = Math.min(entries.size(), start + (listH / ROW_HEIGHT) + 2);

        for (int i = start; i < end; i++) {
            int rowY = listTop + (i * ROW_HEIGHT) - (int) leftScrollOffset;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) {
                continue;
            }

            int bg = i == leftSelectedIndex ? 0xFF2A3A5A : 0xFF252525;
            gg.fill(leftX + INNER_PADDING, rowY, leftX + INNER_PADDING + innerW, rowY + ROW_HEIGHT - 1, bg);

            KeyBindingBackend.BindingEntry entry = entries.get(i);
            int color = colorOf(data.bindingSeverity(entry.id()));
            gg.drawString(font, entry.label(), leftX + INNER_PADDING + 4, rowY + 5, color, false);
        }

        renderScrollbar(gg, true);
    }

    private void renderRightTopList(GuiGraphics gg, int mouseX, int mouseY) {
        int filterX = rightTopX + INNER_PADDING;
        int filterY = rightTopY + TITLE_HEIGHT;
        gg.fill(filterX, filterY, filterX + 92, filterY + 14, 0xFF303030);
        gg.drawString(font, Component.literal("Filter: " + severityFilter.label), filterX + 4, filterY + 3, 0xE0E0E0, false);

        int listTop = filterY + 18;
        int listBottom = rightTopY + rightTopH - INNER_PADDING;
        int listH = listBottom - listTop;
        int innerW = rightTopW - SCROLLBAR_WIDTH - INNER_PADDING * 2;

        List<KeyBindingBackend.PairEntry> entries = getVisiblePairs();
        int start = (int) (pairScrollOffset / ROW_HEIGHT);
        int end = Math.min(entries.size(), start + (listH / ROW_HEIGHT) + 2);

        for (int i = start; i < end; i++) {
            int rowY = listTop + (i * ROW_HEIGHT) - (int) pairScrollOffset;
            if (rowY + ROW_HEIGHT < listTop || rowY > listBottom) {
                continue;
            }

            int bg = i == pairSelectedIndex ? 0xFF2A3A5A : 0xFF252525;
            gg.fill(rightTopX + INNER_PADDING, rowY, rightTopX + INNER_PADDING + innerW, rowY + ROW_HEIGHT - 1, bg);

            KeyBindingBackend.PairEntry e = entries.get(i);
            int color = colorOf(e.severity());
            gg.drawString(font, e.a() + " ↔ " + e.b(), rightTopX + INNER_PADDING + 4, rowY + 1, color, false);
            gg.drawString(font, e.summary(), rightTopX + INNER_PADDING + 4, rowY + 10, 0xC0C0C0, false);
        }

        if (entries.isEmpty() && leftSelectedIndex >= 0) {
            gg.drawString(font,
                          Component.literal("No visible pair under current filter"),
                          rightTopX + INNER_PADDING,
                          listTop + 4,
                          0xAAAAAA,
                          false);
        }

        renderScrollbar(gg, false);
    }

    private void renderRightBottom(GuiGraphics gg) {
        int contentX = rightBottomX + INNER_PADDING;
        int contentY = rightBottomY + TITLE_HEIGHT + 2;
        KeyBindingBackend.PairEntry selected = getSelectedPair();
        if (selected == null) {
            gg.drawString(font, Component.literal("Select a pair to view details."), contentX, contentY, 0xAAAAAA, false);
            return;
        }

        gg.drawString(font, Component.literal("Pair: " + selected.a() + " ↔ " + selected.b()), contentX, contentY, 0xFFFFFF, false);
        gg.drawString(font, Component.literal("Severity: " + selected.severity()), contentX, contentY + 12, colorOf(selected.severity()), false);
        gg.drawString(font, Component.literal("Summary: " + selected.summary()), contentX, contentY + 24, 0xD0D0D0, false);
    }

    private boolean moveSelection(int size, int keyCode, boolean left) {
        if (size <= 0) {
            return false;
        }

        int index = left ? leftSelectedIndex : pairSelectedIndex;
        if (index < 0) {
            index = 0;
        }
        switch (keyCode) {
            case 264 -> index = Math.min(size - 1, index + 1); // down
            case 265 -> index = Math.max(0, index - 1); // up
            case 267 -> index = Math.min(size - 1, index + 8); // pgdown
            case 266 -> index = Math.max(0, index - 8); // pgup
            case 268 -> index = 0; // home
            case 269 -> index = size - 1; // end
            default -> {
                return false;
            }
        }

        if (left) {
            selectLeft(index);
        }
        else {
            selectPair(index, true);
        }
        return true;
    }

    private boolean handleFilterClick(double mouseX, double mouseY) {
        int filterX = rightTopX + INNER_PADDING;
        int filterY = rightTopY + TITLE_HEIGHT;
        if (!isInside(filterX, filterY, 92, 14, mouseX, mouseY)) {
            return false;
        }

        severityFilter = severityFilter == SeverityFilter.WARNING_AND_ABOVE
                         ? SeverityFilter.ALL
                         : SeverityFilter.WARNING_AND_ABOVE;
        pairScrollOffset = 0;
        pairSelectedIndex = getVisiblePairs().isEmpty() ? -1 : 0;
        return true;
    }

    private boolean selectLeftByMouse(double mouseX, double mouseY) {
        int listTop = leftY + TITLE_HEIGHT;
        int listBottom = leftY + leftH - INNER_PADDING;
        if (mouseY < listTop || mouseY > listBottom) {
            return false;
        }

        int row = (int) ((mouseY - listTop + leftScrollOffset) / ROW_HEIGHT);
        if (row >= 0 && row < data.bindings().size()) {
            selectLeft(row);
            return true;
        }
        return false;
    }

    private boolean selectPairByMouse(double mouseX, double mouseY) {
        int listTop = rightTopY + TITLE_HEIGHT + 18;
        int listBottom = rightTopY + rightTopH - INNER_PADDING;
        if (mouseY < listTop || mouseY > listBottom) {
            return false;
        }

        List<KeyBindingBackend.PairEntry> entries = getVisiblePairs();
        int row = (int) ((mouseY - listTop + pairScrollOffset) / ROW_HEIGHT);
        if (row >= 0 && row < entries.size()) {
            selectPair(row, true);
            return true;
        }
        return false;
    }

    private void selectLeft(int index) {
        leftSelectedIndex = clampInt(index, 0, Math.max(0, data.bindings().size() - 1));
        ensureLeftVisible(leftSelectedIndex);

        // If the selected binding has no entries under current severity filter,
        // fallback to global list as requested.
        String selectedId = data.bindings().get(leftSelectedIndex).id();
        if (hasVisibleScopedPairs(selectedId)) {
            pairScopeBindingId = selectedId;
        }
        else {
            pairScopeBindingId = null;
        }

        resetPairListSelection();
    }

    private void selectPair(int index, boolean syncLeft) {
        List<KeyBindingBackend.PairEntry> entries = getVisiblePairs();
        if (entries.isEmpty()) {
            pairSelectedIndex = -1;
            return;
        }

        pairSelectedIndex = clampInt(index, 0, entries.size() - 1);
        ensurePairVisible(pairSelectedIndex);

        if (syncLeft) {
            KeyBindingBackend.PairEntry selected = entries.get(pairSelectedIndex);
            int leftIndex = indexOfBinding(selected.a());
            if (leftIndex >= 0) {
                leftSelectedIndex = leftIndex;
                ensureLeftVisible(leftIndex);
            }
        }
    }

    private void ensureLeftVisible(int index) {
        int listTop = leftY + TITLE_HEIGHT;
        int listBottom = leftY + leftH - INNER_PADDING;
        int viewH = listBottom - listTop;

        double top = index * ROW_HEIGHT;
        double bottom = top + ROW_HEIGHT;
        if (top < leftScrollOffset) {
            leftScrollOffset = top;
        }
        else if (bottom > leftScrollOffset + viewH) {
            leftScrollOffset = bottom - viewH;
        }
        leftScrollOffset = clamp(leftScrollOffset, 0, maxLeftScroll());
    }

    private void ensurePairVisible(int index) {
        int listTop = rightTopY + TITLE_HEIGHT + 18;
        int listBottom = rightTopY + rightTopH - INNER_PADDING;
        int viewH = listBottom - listTop;

        double top = index * ROW_HEIGHT;
        double bottom = top + ROW_HEIGHT;
        if (top < pairScrollOffset) {
            pairScrollOffset = top;
        }
        else if (bottom > pairScrollOffset + viewH) {
            pairScrollOffset = bottom - viewH;
        }
        pairScrollOffset = clamp(pairScrollOffset, 0, maxPairScroll());
    }

    private boolean tryStartScrollbarDrag(boolean left, double mouseX, double mouseY) {
        ThumbRect thumb = left ? leftThumbRect() : pairThumbRect();
        if (thumb == null || !isInside(thumb.x, thumb.y, thumb.w, thumb.h, mouseX, mouseY)) {
            return false;
        }

        if (left) {
            leftScrollbarDragging = true;
            leftDragGrabOffsetY = mouseY - thumb.y;
        }
        else {
            pairScrollbarDragging = true;
            pairDragGrabOffsetY = mouseY - thumb.y;
        }
        return true;
    }

    private void updateScrollFromThumbDrag(boolean left, double mouseY) {
        int trackY;
        int trackH;
        double maxScroll;
        ThumbRect thumb = left ? leftThumbRect() : pairThumbRect();
        if (thumb == null) {
            return;
        }

        if (left) {
            trackY = leftY + TITLE_HEIGHT;
            trackH = leftH - TITLE_HEIGHT - INNER_PADDING;
            maxScroll = maxLeftScroll();
        }
        else {
            trackY = rightTopY + TITLE_HEIGHT + 18;
            trackH = rightTopH - TITLE_HEIGHT - 18 - INNER_PADDING;
            maxScroll = maxPairScroll();
        }

        if (maxScroll <= 0) {
            return;
        }

        double grab = left ? leftDragGrabOffsetY : pairDragGrabOffsetY;
        double newThumbY = clamp(mouseY - grab, trackY, trackY + trackH - thumb.h);
        double ratio = (newThumbY - trackY) / (double) (trackH - thumb.h);
        double scroll = ratio * maxScroll;

        if (left) {
            leftScrollOffset = scroll;
        }
        else {
            pairScrollOffset = scroll;
        }
    }

    private void renderScrollbar(GuiGraphics gg, boolean left) {
        ThumbRect thumb = left ? leftThumbRect() : pairThumbRect();
        if (thumb == null) {
            return;
        }

        gg.fill(thumb.trackX, thumb.trackY, thumb.trackX + thumb.w, thumb.trackY + thumb.trackH, 0xFF2F2F2F);
        gg.fill(thumb.x, thumb.y, thumb.x + thumb.w, thumb.y + thumb.h, 0xFF8A8A8A);
    }

    private ThumbRect leftThumbRect() {
        int listTop = leftY + TITLE_HEIGHT;
        int trackH = leftH - TITLE_HEIGHT - INNER_PADDING;
        int trackX = leftX + leftW - SCROLLBAR_WIDTH - INNER_PADDING;
        return buildThumbRect(trackX, listTop, trackH, data.bindings().size(), leftScrollOffset, maxLeftScroll());
    }

    private ThumbRect pairThumbRect() {
        int listTop = rightTopY + TITLE_HEIGHT + 18;
        int trackH = rightTopH - TITLE_HEIGHT - 18 - INNER_PADDING;
        int trackX = rightTopX + rightTopW - SCROLLBAR_WIDTH - INNER_PADDING;
        return buildThumbRect(trackX, listTop, trackH, getVisiblePairs().size(), pairScrollOffset, maxPairScroll());
    }

    private ThumbRect buildThumbRect(int trackX, int trackY, int trackH, int itemCount, double scroll, double maxScroll) {
        int contentH = itemCount * ROW_HEIGHT;
        if (contentH <= trackH || trackH <= 0) {
            return null;
        }

        int thumbH = Math.max(MIN_THUMB_HEIGHT, (int) ((trackH * (double) trackH) / contentH));
        int travel = trackH - thumbH;
        int thumbY = trackY + (maxScroll <= 0 ? 0 : (int) (travel * (scroll / maxScroll)));
        return new ThumbRect(trackX, trackY, SCROLLBAR_WIDTH, trackH, trackX, thumbY, thumbH);
    }

    private double maxLeftScroll() {
        int listH = leftH - TITLE_HEIGHT - INNER_PADDING;
        return Math.max(0, data.bindings().size() * ROW_HEIGHT - listH);
    }

    private double maxPairScroll() {
        int listH = rightTopH - TITLE_HEIGHT - 18 - INNER_PADDING;
        return Math.max(0, getVisiblePairs().size() * ROW_HEIGHT - listH);
    }

    private List<KeyBindingBackend.PairEntry> getVisiblePairs() {
        List<KeyBindingBackend.PairEntry> visible = new java.util.ArrayList<>();
        for (KeyBindingBackend.PairEntry pair : data.pairs()) {
            if (pair.severity().ordinal() < severityFilter.threshold().ordinal()) {
                continue;
            }

            if (pairScopeBindingId != null && !pair.a().equals(pairScopeBindingId) && !pair.b().equals(pairScopeBindingId)) {
                continue;
            }
            visible.add(pair);
        }
        return visible;
    }

    private boolean hasVisibleScopedPairs(String bindingId) {
        for (KeyBindingBackend.PairEntry pair : data.pairs()) {
            if (pair.severity().ordinal() < severityFilter.threshold().ordinal()) {
                continue;
            }
            if (pair.a().equals(bindingId) || pair.b().equals(bindingId)) {
                return true;
            }
        }
        return false;
    }

    private void clearRightScopeToGlobal() {
        pairScopeBindingId = null;
        leftSelectedIndex = -1;
        resetPairListSelection();
    }

    private void resetPairListSelection() {
        pairScrollOffset = 0;
        List<KeyBindingBackend.PairEntry> visible = getVisiblePairs();
        pairSelectedIndex = visible.isEmpty() ? -1 : 0;
    }

    private KeyBindingBackend.PairEntry getSelectedPair() {
        List<KeyBindingBackend.PairEntry> visible = getVisiblePairs();
        if (pairSelectedIndex < 0 || pairSelectedIndex >= visible.size()) {
            return null;
        }
        return visible.get(pairSelectedIndex);
    }

    private int indexOfBinding(String id) {
        for (int i = 0; i < data.bindings().size(); i++) {
            if (data.bindings().get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void onActionJumpToA() {
        KeyBindingBackend.PairEntry selected = getSelectedPair();
        if (selected == null) return;
        int idx = indexOfBinding(selected.a());
        if (idx >= 0) {
            leftSelectedIndex = idx;
            ensureLeftVisible(idx);
        }
    }

    private void onActionJumpToB() {
        KeyBindingBackend.PairEntry selected = getSelectedPair();
        if (selected == null) return;
        int idx = indexOfBinding(selected.b());
        if (idx >= 0) {
            leftSelectedIndex = idx;
            ensureLeftVisible(idx);
        }
    }

    private void onActionExclude() {
        KeyBindingBackend.PairEntry selected = getSelectedPair();
        if (selected != null) {
            actions.onExclude(selected);
        }
    }

    private void onActionPromoteRule() {
        KeyBindingBackend.PairEntry selected = getSelectedPair();
        if (selected != null) {
            actions.onPromoteRule(selected);
        }
    }

    private void recalculateLayout() {
        leftX = OUTER_PADDING;
        leftY = OUTER_PADDING;
        leftW = (int) (this.width * 0.6) - OUTER_PADDING * 2;
        leftH = this.height - OUTER_PADDING * 2;

        rightX = leftX + leftW + OUTER_PADDING;
        rightY = OUTER_PADDING;
        rightW = this.width - rightX - OUTER_PADDING;
        rightH = this.height - OUTER_PADDING * 2;

        rightTopX = rightX + INNER_PADDING;
        rightTopY = rightY + INNER_PADDING;
        rightTopW = rightW - INNER_PADDING * 2;
        rightTopH = (int) (rightH * 0.45) - INNER_PADDING;

        rightBottomX = rightX + INNER_PADDING;
        rightBottomY = rightTopY + rightTopH + INNER_PADDING;
        rightBottomW = rightW - INNER_PADDING * 2;
        rightBottomH = rightH - rightTopH - INNER_PADDING * 3;
    }

    private static int colorOf(Severity severity) {
        return switch (severity) {
            case SAFE -> 0xA0A0A0;
            case INFO -> 0x6DA8FF;
            case WARNING -> 0xFFB020;
            case SEVERE -> 0xFF5A5A;
        };
    }

    private static boolean isInside(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum FocusPanel {
        LEFT_LIST,
        RIGHT_TOP_LIST,
        NONE
    }

    private enum SeverityFilter {
        WARNING_AND_ABOVE("WARNING+", Severity.WARNING),
        ALL("ALL", Severity.SAFE);

        private final String label;
        private final Severity threshold;

        SeverityFilter(String label, Severity threshold) {
            this.label = label;
            this.threshold = threshold;
        }

        Severity threshold() {
            return threshold;
        }

        boolean matches(Severity severity) {
            return severity.ordinal() >= threshold.ordinal();
        }
    }


    private record ThumbRect(int trackX, int trackY, int w, int trackH, int x, int y, int h) {
    }
}

package com.lnatit.kord.gui.front;

import com.lnatit.kord.gui.Backend;
import com.lnatit.kord.result.ConflictResult;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

public class KeyBindingSelectionListWidget extends AbstractSelectionList<KeyBindingSelectionListWidget.@NotNull ListEntry>
{
    private static final int ROW_H = 20;
    private static final int SCROLLBAR_W = 4;
    private static final int TEXT_PAD = 4;
    private static final long TEXT_SCROLL_PERIOD_MS = 5000L;

    private static final int COL_HOVER = 0x60404040;
    private static final int COL_SELECTED = 0x803366FF;
    private static final int COL_SEVERE = 0x80FF2222;
    private static final int COL_WARNING = 0x80FFAA00;
    private static final int COL_INFO = 0x80FFFF44;

    private final List<KeyMapping> keys;
    private final List<BindingEntry> bindingEntries = new ArrayList<>();
    private final Font font;
    private final IntConsumer onKeySelected;
    private final IntConsumer onBindingLeftClick;
    private final IntConsumer onBindingRightClick;

    private int selectedIndex = -1;

    public KeyBindingSelectionListWidget(
            Minecraft minecraft,
            Font font,
            int x,
            int y,
            int width,
            int height,
            IntConsumer onKeySelected,
            IntConsumer onBindingLeftClick,
            IntConsumer onBindingRightClick
    ) {
        super(minecraft, width, height, y, ROW_H);
        this.font = font;
        this.onKeySelected = onKeySelected;
        this.onBindingLeftClick = onBindingLeftClick;
        this.onBindingRightClick = onBindingRightClick;
        this.updateSizeAndPosition(width, height, x, y);
        KeyMapping[] keyMappings = ArrayUtils.clone(minecraft.options.keyMappings);
        Arrays.sort(keyMappings);
        this.keys = List.of(keyMappings);
        this.rebuildEntries();
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    @Nullable
    public KeyMapping getSelectedKey() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.bindingEntries.size()
               ? this.bindingEntries.get(this.selectedIndex).keyMapping
               : null;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= this.bindingEntries.size()) {
            this.setSelected(null);
            return;
        }

        this.setSelected(this.bindingEntries.get(index));
    }

    public void clearSelection() {
        this.setSelected(null);
    }

    @Override
    public void setSelected(@Nullable ListEntry selected) {
        super.setSelected(selected);
        this.selectedIndex = selected instanceof BindingEntry binding ? binding.index : -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.clearSelection();
        return super.mouseClicked(event, doubleClick);
    }

    private void rebuildEntries() {
        this.clearEntries();
        this.bindingEntries.clear();

        KeyMapping.Category previousCategory = null;
        for (int i = 0; i < this.keys.size(); i++) {
            KeyMapping key = this.keys.get(i);
            KeyMapping.Category category = key.getCategory();
            if (category != previousCategory) {
                previousCategory = category;
                this.addEntry(new CategoryEntry(category), ROW_H);
            }

            BindingEntry entry = new BindingEntry(i, key);
            this.bindingEntries.add(entry);
            this.addEntry(entry, ROW_H);
        }

        if (this.selectedIndex >= 0 && this.selectedIndex < this.bindingEntries.size()) {
            this.setSelected(this.bindingEntries.get(this.selectedIndex));
        }
        else {
            this.setSelected(null);
        }
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractSelection(GuiGraphicsExtractor graphics, ListEntry entry, int outlineColor) {
        if (entry instanceof BindingEntry) {
            graphics.fill(entry.getX(),
                          entry.getY(),
                          entry.getX() + entry.getWidth(),
                          entry.getY() + entry.getHeight(),
                          COL_SELECTED);
        }
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    public int getRowWidth() {
        return Math.max(0, this.getWidth() - SCROLLBAR_W);
    }

    @Override
    public int scrollbarWidth() {
        return SCROLLBAR_W;
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.getWidth() - this.scrollbarWidth();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    private static int severityTint(Severity severity) {
        return switch (severity) {
            case SEVERE -> COL_SEVERE;
            case WARNING -> COL_WARNING;
            case INFO -> COL_INFO;
            default -> 0;
        };
    }

    private void drawLeftScrollingText(GuiGraphicsExtractor graphics,
                                       Component text,
                                       int left,
                                       int top,
                                       int right,
                                       int bottom,
                                       int color) {
        int width = Math.max(0, right - left);
        if (width <= 0) {
            return;
        }

        int textWidth = this.font.width(text);
        if (textWidth <= width) {
            graphics.text(this.font, text, left, top + (bottom - top - 9) / 2, color);
            return;
        }

        int overflow = textWidth - width;
        long time = Math.floorMod(System.currentTimeMillis(), TEXT_SCROLL_PERIOD_MS);
        double phase = (double) time / TEXT_SCROLL_PERIOD_MS;
        double triangle = phase < 0.5D ? phase * 2.0D : (1.0D - phase) * 2.0D;
        int offset = (int) Math.round(overflow * triangle);

        graphics.enableScissor(left, top, right, bottom);
        graphics.text(this.font, text, left - offset, top + (bottom - top - 9) / 2, color);
        graphics.disableScissor();
    }

    public abstract static class ListEntry extends AbstractSelectionList.Entry<ListEntry>
    {
    }

    public class CategoryEntry extends ListEntry
    {
        private final KeyMapping.Category category;

        CategoryEntry(KeyMapping.Category category) {
            this.category = category;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return false;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int textY = this.getY() + (this.getHeight() - 9) / 2;
            graphics.centeredText(KeyBindingSelectionListWidget.this.font,
                                  this.category.label(),
                                  this.getX() + this.getWidth() / 2,
                                  textY,
                                  0xFFDDDDDD);
        }
    }

    public class BindingEntry extends ListEntry
    {
        private final int index;
        private final KeyMapping keyMapping;
        private final Severity severity;

        BindingEntry(int index, KeyMapping keyMapping) {
            this.index = index;
            this.keyMapping = keyMapping;
            this.severity = Backend.filter(Backend.byKeyMapping(keyMapping)).stream()
                                  .map(ConflictResult::severity)
                                  .max(Severity::compareTo)
                                  .orElse(Severity.SAFE);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!this.isMouseOver(event.x(), event.y())) {
                return false;
            }

            KeyBindingSelectionListWidget.this.setSelected(this);
            KeyBindingSelectionListWidget.this.onKeySelected.accept(this.index);

            int bindBtnX = this.getX() + this.getWidth() / 2;
            if (event.x() >= bindBtnX) {
                if (event.button() == 0) {
                    KeyBindingSelectionListWidget.this.onBindingLeftClick.accept(this.index);
                }
                else if (event.button() == 1) {
                    KeyBindingSelectionListWidget.this.onBindingRightClick.accept(this.index);
                }
            }
            return true;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int rowX = this.getX();
            int rowY = this.getY();
            int rowW = this.getWidth();
            int rowH = this.getHeight();

            if (hovered) {
                graphics.fill(rowX, rowY, rowX + rowW, rowY + rowH, COL_HOVER);
            }

            int bindBtnX = rowX + rowW / 2;
            int bindBtnW = rowW - rowW / 2;

            int tint = severityTint(this.severity);
            if (tint != 0) {
                graphics.fill(bindBtnX, rowY, rowX + rowW, rowY + rowH, tint);
            }

            if (mouseX >= bindBtnX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + rowH) {
                graphics.fill(bindBtnX, rowY, rowX + rowW, rowY + rowH, COL_HOVER);
            }

            int textY = rowY + (rowH - 9) / 2;
            KeyBindingSelectionListWidget.this.drawLeftScrollingText(
                    graphics,
                    this.keyMapping.getDisplayName(),
                    rowX + TEXT_PAD,
                    rowY,
                    bindBtnX - TEXT_PAD,
                    rowY + rowH,
                    0xFFEEEEEE
            );
            graphics.centeredText(KeyBindingSelectionListWidget.this.font,
                                  this.keyMapping.getTranslatedKeyMessage(),
                                  bindBtnX + bindBtnW / 2,
                                  textY,
                                  0xFFFFFFFF);

            graphics.fill(rowX, rowY + rowH - 1, rowX + rowW, rowY + rowH, 0x30FFFFFF);
        }
    }
}


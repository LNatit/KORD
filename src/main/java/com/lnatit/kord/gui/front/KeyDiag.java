package com.lnatit.kord.gui.front;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.lnatit.kord.Kord;
import com.lnatit.kord.gui.Backend;
import com.lnatit.kord.result.ConflictResult;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KeyDiag extends Screen
{
    public static final int MIN_WIDTH = 320;
    public static final int MIN_HEIGHT = 240;

    public static final int PADDING = 6;
    public static final int SIDEBAR_ICON_SIZE = 24;
    public static final int ENTRY_HEIGHT = 20;
    public static final int DETAIL_BUTTON_WIDTH = 48;
    public static final int TOTAL_WIDTH = 9;
    public static final int BINDINGLIST_WIDTH_RATIO = 5;
    public static final int TEXT_PAD = 4;

    public static final int BACKGROUND_COLOR = 0x80101010;

    private final Screen previousScreen;

    private final Sidebar sidebar = new Sidebar();
    private final BindingList bindingList = new BindingList();
    private final Diagnostics diagnostics = new Diagnostics();

    public KeyDiag(Screen previous) {
        super(Component.empty());
        this.previousScreen = previous;
    }

    @Override
    protected void init() {
        this.sidebar.init();
        this.addRenderableOnly(this.sidebar);
        this.addRenderableChild(this.sidebar.exitButton);
        this.addRenderableChild(this.sidebar.settingsButton);
        this.addRenderableChild(this.sidebar.reloadButton);
        this.addRenderableChild(this.sidebar.refreshButton);

        this.bindingList.init();
        this.addRenderableOnly(this.bindingList);
        this.addRenderableChild(this.bindingList.searchBar);
        this.addRenderableChild(this.bindingList.bindings);

        this.diagnostics.init();
        this.addRenderableOnly(this.diagnostics);
        this.addRenderableOnly(this.diagnostics.filter);
        this.addRenderableChild(this.diagnostics.conflicts);
        this.addRenderableOnly(this.diagnostics.detail);
        this.addRenderableChild(this.diagnostics.detail.navigateButton);
        this.addRenderableChild(this.diagnostics.detail.editButton);

        this.repositionElements();
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableChild(T child) {
        ((List<GuiEventListener>) this.children()).add(child);
        return super.addRenderableOnly(child);
    }

    @Override
    protected void repositionElements() {
        if (isSmallerThanMinimumSize()) {
            this.setFocused(false);


            // TODO add a tooltip
            return;
        }

        this.sidebar.repositionElements();
        this.bindingList.repositionElements();
        this.diagnostics.repositionElements();
    }

    private void onKeySelected(BindingList.Bindings.KeyEntry entry) {
        // TODO: rebuild Conflicts list for entry.keyMapping
    }

    private void onConflictSelected(Diagnostics.Conflicts.Entry entry) {
        // TODO: refresh Detail panel for entry.result
    }

    private boolean isSmallerThanMinimumSize() {
        return this.width < MIN_WIDTH || this.height < MIN_HEIGHT;
    }

    private int sidebarX() {
        return PADDING;
    }

    private int sidebarWidth() {
        return SIDEBAR_ICON_SIZE;
    }

    private int bindingListX() {
        return this.sidebarX() + this.sidebarWidth() + PADDING;
    }

    private int bindingListWidth() {
        return ((this.width - this.sidebarWidth() - PADDING * 4) * BINDINGLIST_WIDTH_RATIO / TOTAL_WIDTH);
    }

    private int diagnosticsX() {
        return this.bindingListX() + this.bindingListWidth() + PADDING;
    }

    private int diagnosticsWidth() {
        return this.width - this.diagnosticsX() - PADDING;
    }

    private class Sidebar extends Panel implements Reloadable
    {
        public static final int BACKGROUND_COLOR = 0xFF101010;

        public static final Identifier EXIT_DEFAULT = Kord.id("widget/exit_default");
        public static final Identifier EXIT_SELECTED = Kord.id("widget/exit_selected");
        public static final Identifier REFRESH_DEFAULT = Kord.id("widget/refresh_default");
        public static final Identifier REFRESH_SELECTED = Kord.id("widget/refresh_selected");
        public static final Identifier RELOAD_DEFAULT = Kord.id("widget/reload_default");
        public static final Identifier RELOAD_SELECTED = Kord.id("widget/reload_selected");
        public static final Identifier SETTINGS_DEFAULT = Kord.id("widget/settings_default");
        public static final Identifier SETTINGS_SELECTED = Kord.id("widget/settings_selected");

        public static final Component EXIT_MSG = Component.translatable("gui.kord.exit");
        public static final Component SETTINGS_MSG = Component.translatable("gui.kord.settings");
        public static final Component RELOAD_MSG = Component.translatable("gui.kord.reload");
        public static final Component REFRESH_MSG = Component.translatable("gui.kord.refresh");

        private final SpriteBtn exitButton = new SpriteBtn(EXIT_DEFAULT, EXIT_SELECTED, EXIT_MSG)
        {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {
                Minecraft.getInstance().setScreen(previousScreen);
            }
        };
        private final SpriteBtn settingsButton = new SpriteBtn(SETTINGS_DEFAULT, SETTINGS_SELECTED, SETTINGS_MSG)
        {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {

            }
        };
        private final SpriteBtn reloadButton = new SpriteBtn(RELOAD_DEFAULT, RELOAD_SELECTED, RELOAD_MSG)
        {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {

            }
        };
        private final SpriteBtn refreshButton = new SpriteBtn(REFRESH_DEFAULT, REFRESH_SELECTED, REFRESH_MSG)
        {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {

            }
        };

        @Override
        public void repositionElements() {
            this.setX(KeyDiag.this.sidebarX());
            this.setY(0);
            this.setWidth(KeyDiag.this.sidebarWidth());
            this.setHeight(KeyDiag.this.height);
            this.exitButton.setPosition(this.getX(), this.getY());
            this.settingsButton.setPosition(this.getX(), this.getBottom() - this.settingsButton.getHeight());
            this.reloadButton.setPosition(this.getX(), this.settingsButton.getY() - this.reloadButton.getHeight());
            this.refreshButton.setPosition(this.getX(), this.reloadButton.getY() - this.refreshButton.getHeight());
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_COLOR);
        }
    }

    private class BindingList extends Panel implements Reloadable
    {
        private final Bindings bindings = new Bindings();
        private final SearchBar searchBar = new SearchBar();

        @Override
        public void init() {
            this.bindings.init();
//            this.searchBar.init();
        }

        @Override
        public void repositionElements() {
            this.setX(KeyDiag.this.bindingListX());
            this.setY(0);
            this.setWidth(KeyDiag.this.bindingListWidth());
            this.setHeight(KeyDiag.this.height);

            int listHeight = this.getHeight() - ENTRY_HEIGHT;
            this.bindings.setRectangle(this.getWidth(), listHeight, this.getX(), this.getY());
            this.bindings.repositionElements();

            this.searchBar.setRectangle(this.getWidth(), ENTRY_HEIGHT, this.getX(), this.bindings.getBottom());
//            this.searchBar.repositionElements();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        }
        // Bindings
        // SearchBar

        private class Bindings extends ListWidget<Bindings.KeyEntry>
        {
            public Bindings() {
                super(ENTRY_HEIGHT);
            }

            @Override
            public void init() {
                for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
                    this.addEntry(new KeyEntry(key));
                }
                super.init();
            }

            @Override
            protected void extractListBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                graphics.fill(this.getX(), this.getY(), this.scrollBarX(), this.getBottom(), BACKGROUND_COLOR);
            }

            private final class CategoryEntry extends ListWidget.Entry
            {
                public static final int CATEGORY_COLOR = 0xFFDDDDDD;

                private final KeyMapping.Category category;

                private CategoryEntry(KeyMapping.Category category) {
                    this.category = category;
                }

                @Override
                public void extractContentState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    graphics.centeredText(KeyDiag.this.font,
                                          category.label(),
                                          this.getX() + this.getWidth() / 2,
                                          this.getY() + (this.getHeight() - 8) / 2,
                                          CATEGORY_COLOR);
                }

                @Override
                public boolean isFocused() {
                    return false;
                }
            }

            private final class KeyEntry extends ListWidget.Entry implements Selectable
            {
                public static final Component KEYBIND_HINT = Component.translatable("gui.kord.keybind_hint");

                private final KeyMapping keyMapping;
                private Severity severity = Severity.SAFE;

                public KeyEntry(KeyMapping keyMapping) {
                    super();
                    this.keyMapping = keyMapping;
                    this.refreshSeverity();
                    this.setTooltip(Tooltip.create(KEYBIND_HINT));
                }

                @Override
                public void extractContentState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    int rowX = this.getX();
                    int rowY = this.getY();
                    int rowW = this.getWidth();
                    int rowH = this.getHeight();

                    int bindBtnX = rowX + rowW / 2;
                    int bindBtnW = rowW - rowW / 2;

                    boolean hoveredKey = mouseX >= bindBtnX
                                         && mouseX < bindBtnX + bindBtnW
                                         && mouseY >= rowY
                                         && mouseY < rowY + rowH;
                    boolean hovered = this.isMouseOver(mouseX, mouseY);
                    if (hovered) {
                        if (hoveredKey) {
                            graphics.fill(bindBtnX, rowY, bindBtnX + bindBtnW, rowY + rowH, BG_HOVER);
                        }
                        else {
                            graphics.fill(rowX, rowY, rowX + rowW, rowY + rowH, BG_HOVER);
                        }
                    }

                    int tint = severityTint(this.severity);
                    if (tint != 0) {
                        graphics.fill(bindBtnX, rowY, rowX + rowW, rowY + rowH, tint);
                    }

                    if (this.isFocused()) {
                        graphics.outline(rowX, rowY, rowW, rowH, 0xFFFFFFFF);
                    }

                    int textY = rowY + (rowH - 8) / 2;
                    KeyDiag.drawMouseCtrledScrollingText(graphics,
                                                         KeyDiag.this.font,
                                                         this.keyMapping.getDisplayName(),
                                                         rowX,
                                                         rowY,
                                                         bindBtnX,
                                                         rowY + rowH,
                                                         mouseX,
                                                         mouseY,
                                                         0xFFEEEEEE);
                    graphics.centeredText(KeyDiag.this.font,
                                          this.keyMapping.getTranslatedKeyMessage(),
                                          bindBtnX + bindBtnW / 2,
                                          textY,
                                          0xFFFFFFFF);
                }

                @Override
                public ScreenRectangle getTooltipArea() {
                    int rowX = this.getX();
                    int rowY = this.getY();
                    int rowW = this.getWidth();
                    int rowH = this.getHeight();

                    int bindBtnX = rowX + rowW / 2;
                    int bindBtnW = rowW - rowW / 2;
                    return new ScreenRectangle(bindBtnX, rowY, bindBtnW, rowH);
                }

                @Override
                public void select() {
                    KeyDiag.this.setFocused(this);
                }

                @Override
                public boolean isFocused() {
                    return KeyDiag.this.getFocused() == this;
                }

                private void refreshSeverity() {
                    this.severity = Backend.filter(Backend.byKeyMapping(keyMapping))
                                           .stream()
                                           .map(ConflictResult::severity)
                                           .max(Severity::compareTo)
                                           .orElse(Severity.SAFE);
                }
            }
        }

        private class SearchBar extends EditBox
        {
            public static final int BACKGROUND_COLOR = 0xC0000000;
            public static final Component HINT = Component.translatable("gui.kord.search_keys");

            public SearchBar() {
                super(KeyDiag.this.font, 0, 0, 0, 20, BACKGROUND_COLOR);
                this.setBordered(false);
                this.setHint(HINT);
            }

            @Override
            protected void onUpdate(String value) {
            }
        }
    }

    private class Diagnostics extends Panel implements Reloadable
    {
        public static final int TOTAL_HEIGHT = 2;
        public static final int LIST_HIGHT_RATIO = 1;

        private final Filter filter = new Filter();
        private final Conflicts conflicts = new Conflicts();
        private final Detail detail = new Detail();

        public Diagnostics() {
            super();
        }

        @Override
        public void init() {
            this.filter.init();
            this.conflicts.init();
            this.detail.init();
        }

        @Override
        public void repositionElements() {
            this.setX(KeyDiag.this.diagnosticsX());
            this.setY(0);
            this.setWidth(KeyDiag.this.diagnosticsWidth());
            this.setHeight(KeyDiag.this.height);

            this.filter.repositionElements();

            int listY = this.filter.getBottom();
            int listHeight = (this.getHeight() - this.filter.getHeight()) * LIST_HIGHT_RATIO / TOTAL_HEIGHT;
            this.conflicts.setRectangle(this.getWidth(), listHeight, this.getX(), listY);
            this.conflicts.repositionElements();

            this.detail.repositionElements();
        }

        private class Filter extends Panel implements Reloadable
        {
            public static final int BACKGROUND_COLOR = 0xC0000000;

            @Override
            public void repositionElements() {
                this.setX(Diagnostics.this.getX());
                this.setY(Diagnostics.this.getY());
                this.setWidth(Diagnostics.this.getWidth());
                this.setHeight(ENTRY_HEIGHT);
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_COLOR);
            }


            // ToggleBtn ...
        }

        private class Conflicts extends ListWidget<Conflicts.Entry>
        {
            public static final int ENTRY_HEIGHT = 12;

            public static final Component SPLITTER = Component.literal("↔");

            public Conflicts() {
                super(ENTRY_HEIGHT);
            }

            @Override
            public void init() {
                for (ConflictResult result : Backend.filter()) {
                    this.addEntry(new Entry(result));
                }
                super.init();
            }

            @Override
            protected void extractListBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                graphics.fill(this.getX(), this.getY(), this.scrollBarX(), this.getBottom(), BACKGROUND_COLOR);
            }

            private final class Entry extends ListWidget.Entry implements Selectable
            {
                private final ConflictResult result;

                public Entry(ConflictResult result) {
                    super();
                    this.result = result;
                }

                @Override
                public void extractContentState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    Font font = KeyDiag.this.font;
                    int rowX = this.getX();
                    int rowY = this.getY();
                    int rowW = this.getWidth();
                    int rowH = this.getHeight();

                    if (this.isMouseOver(mouseX, mouseY)) {
                        graphics.fill(rowX, rowY, rowX + rowW, rowY + rowH, BG_HOVER);
                    }

                    if (this.isFocused()) {
                        graphics.outline(rowX, rowY, rowW, rowH, 0xFFFFFFFF);
                    }


                    int innerLeft = rowX + TEXT_PAD;
                    int innerRight = rowX + rowW - TEXT_PAD;

                    int splitterWidth = font.width(SPLITTER);
                    int splitterOuter = splitterWidth + TEXT_PAD * 2;

                    int splitterX = rowX + rowW / 2;
                    int splitterLeft = splitterX - splitterOuter / 2;
                    int splitterRight = splitterLeft + splitterOuter;
                    int leftX = (innerLeft + splitterLeft) / 2;
                    int rightX = (innerRight + splitterRight) / 2;
                    int textY = rowY + (rowH - 8) / 2;

                    // TODO scissor and scrolling
                    graphics.centeredText(font, this.result.pair().left().getDisplayName(), leftX, textY, 0xFFEEEEEE);
                    graphics.centeredText(font, SPLITTER, splitterX, textY, 0xFF888888);
                    graphics.centeredText(font, this.result.pair().right().getDisplayName(), rightX, textY, 0xFFEEEEEE);
                }

                @Override
                public void select() {
                    KeyDiag.this.setFocused(this);
                }

                @Override
                public boolean isFocused() {
                    return KeyDiag.this.getFocused() == this;
                }
            }
        }

        private class Detail extends Panel implements Reloadable
        {
            public static final int BUTTON_DEFAULT_COLOR = BACKGROUND_COLOR;
            public static final int BUTTON_SELECTED_COLOR = 0x30FFFFFF;

            private final ColorBtn navigateButton = new ColorBtn(KeyDiag.this.font,
                                                                 BUTTON_DEFAULT_COLOR,
                                                                 BUTTON_SELECTED_COLOR,
                                                                 Component.translatable("gui.kord.navigate"))
            {
                @Override
                public void onClick(MouseButtonEvent event, boolean doubleClick) {

                }
            };
            private final ColorBtn editButton = new ColorBtn(KeyDiag.this.font,
                                                             BUTTON_DEFAULT_COLOR,
                                                             BUTTON_SELECTED_COLOR,
                                                             Component.translatable("gui.kord.edit"))
            {
                @Override
                public void onClick(MouseButtonEvent event, boolean doubleClick) {

                }
            };

            @Override
            public void repositionElements() {
                int detailY = Diagnostics.this.conflicts.getBottom() + PADDING;
                int detailHeight = Diagnostics.this.getBottom() - detailY;
                this.setX(Diagnostics.this.getX());
                this.setY(detailY);
                this.setWidth(Diagnostics.this.getWidth());
                this.setHeight(detailHeight);

                int buttonX = this.getRight() - DETAIL_BUTTON_WIDTH;
                this.navigateButton.setRectangle(DETAIL_BUTTON_WIDTH, ENTRY_HEIGHT, buttonX, this.getY());
                this.editButton.setRectangle(DETAIL_BUTTON_WIDTH, ENTRY_HEIGHT, buttonX, this.getY() + ENTRY_HEIGHT + PADDING);
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                graphics.fill(this.getX(), this.getY(), this.getMessageRight(), this.getBottom(), BACKGROUND_COLOR);
            }

            public int getMessageRight() {
                return this.getRight() - DETAIL_BUTTON_WIDTH - PADDING;
            }


            // Message  NavigateBtn
            //          EditBtn
        }
    }

    private static int severityTint(Severity severity) {
        return switch (severity) {
            case SAFE -> 0;
            case INFO -> 0x400080FF;
            case WARNING -> 0x40FFFF00;
            case SEVERE -> 0x40FF0000;
        };
    }

    /**
     * Draws text clipped to [x1, x2]. If the text is wider than the available space,
     * it scrolls left and right with a short pause at each end.
     *
     * <p>Timing: 30 ms per scroll-pixel, 2 000 ms pause at each end.</p>
     */
    private static void drawScrollingText(
            GuiGraphicsExtractor graphics,
            Font font,
            Component text,
            int x1,
            int y1,
            int x2,
            int y2,
            int color,
            boolean scrolling
    ) {
        int availW = x2 - x1;
        int textW = font.width(text);
        int textY = y1 + (y2 - y1 - 9) / 2;

        if (textW <= availW) {
            graphics.text(font, text, x1, textY, color);
            return;
        }

        int scrollRange = textW - availW;
        long msPerPx = 30L;
        long pause = 2000L;
        long period = scrollRange * msPerPx + pause * 2;
        long t = System.currentTimeMillis() % period;

        int offset;
        if (!scrolling || t < pause) {
            offset = 0;
        }
        else if (t < pause + scrollRange * msPerPx) {
            offset = (int) ((t - pause) / msPerPx);
        }
        else {
            offset = scrollRange;
        }

        graphics.enableScissor(x1, y1, x2, y2);
        graphics.text(font, text, x1 - offset, textY, color);
        graphics.disableScissor();
    }

    private static void drawMouseCtrledScrollingText(
            GuiGraphicsExtractor graphics,
            Font font,
            Component text,
            int x1,
            int y1,
            int x2,
            int y2,
            int mouseX,
            int mouseY,
            int color
    ) {
        int outerW = Math.max(0, x2 - x1);
        int innerX1 = x1 + TEXT_PAD;
        int innerX2 = x2 - TEXT_PAD;
        int innerW = Math.max(0, innerX2 - innerX1);
        if (innerW == 0) {
            return;
        }

        int textW = font.width(text);
        int textY = y1 + (y2 - y1 - 9) / 2;

        // Fits in the available area: draw centered text in the inset region.
        if (textW <= innerW) {
            graphics.centeredText(font, text, innerX1 + innerW / 2, textY, color);
            return;
        }

        int scrollRange = textW - innerW;
        double ratio = Math.clamp((double) innerW / (double) textW, 0.0, 1.0);

        // 0..80% of the outer width depending on how much text overflows.
        int responseW = (int) Math.round(outerW * 0.8 * (1.0 - ratio));

        int offset = 0;
        boolean mouseInside = mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
        if (mouseInside && responseW > 1) {
            int responseX1 = x1 + (outerW - responseW) / 2;
            int responseX2 = responseX1 + responseW;
            double t = Math.clamp((mouseX - responseX1) / (double) (responseX2 - responseX1), 0.0, 1.0);
            t = 1 - t;
            offset = (int) Math.round(scrollRange * t);
        }

        graphics.enableScissor(innerX1, y1, innerX2, y2);
        graphics.text(font, text, innerX1 - offset, textY, color);
        graphics.disableScissor();
    }

    private interface Reloadable
    {
        void repositionElements();

        default void init() {
        }
    }

    private sealed interface Selectable permits BindingList.Bindings.KeyEntry, Diagnostics.Conflicts.Entry
    {
        void select();
    }

    private static abstract class Panel implements LayoutElement, Renderable
    {
        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public Panel() {
            this.x = 0;
            this.y = 0;
            this.width = 0;
            this.height = 0;
        }

        @Override
        public void setX(int x) {
            this.x = x;
        }

        @Override
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setRectangle(int width, int height, int x, int y) {
            this.setWidth(width);
            this.setHeight(height);
            this.setX(x);
            this.setY(y);
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        public int getRight() {
            return this.getX() + this.getWidth();
        }

        public int getBottom() {
            return this.getY() + this.getHeight();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
        }

        protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
            return buttonInfo.button() == 0;
        }

        protected void playDownSound() {
            Minecraft.getInstance()
                     .getSoundManager()
                     .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public abstract static class Widget extends Panel implements GuiEventListener
    {
        @Override
        public ScreenRectangle getRectangle() {
            return super.getRectangle();
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return this.getRectangle().containsPoint(mouseX, mouseY);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.isMouseOver((int) mouseX, (int) mouseY);
        }
    }

    // Clean-room edit box based on Widget, kept minimal for SearchBar migration.
    private static abstract class EditBox extends Widget
    {
        public static final int DEFAULT_TEXT_COLOR = -2039584;
        public static final Style DEFAULT_HINT_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);

        private final Font font;
        private final int backgroundColor;
        private String value = "";
        private int maxLength = 32;
        private boolean bordered = true;
        private boolean canLoseFocus = true;
        private boolean editable = true;
        private boolean textShadow = true;
        private int displayPos;
        private int cursorPos;
        private int highlightPos;
        private int textColor = DEFAULT_TEXT_COLOR;
        private int textColorUneditable = -9408400;
        private @Nullable Component hint;
        private java.util.function.Predicate<String> filter = s -> true;
        private boolean focused;
        private long focusedTime = Util.getMillis();
        private int textX;
        private int textY;

        public EditBox(Font font, int width, int height, int backgroundColor) {
            this(font, 0, 0, width, height, backgroundColor);
        }

        public EditBox(Font font, int x, int y, int width, int height, int backgroundColor) {
            this.font = font;
            this.backgroundColor = backgroundColor;
            this.setRectangle(width, height, x, y);
            this.updateTextPosition();
        }

        public void setFilter(java.util.function.Predicate<String> filter) {
            this.filter = filter;
        }

        public void setValue(String value) {
            value = sanitizeSingleLine(value);
            if (!this.filter.test(value)) {
                return;
            }
            this.value = value.length() > this.maxLength ? value.substring(0, this.maxLength) : value;
            this.moveCursorToEnd(false);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }

        public String getValue() {
            return this.value;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
            if (this.value.length() > maxLength) {
                this.value = this.value.substring(0, maxLength);
                this.onValueChange(this.value);
            }
        }

        public void setBordered(boolean bordered) {
            this.bordered = bordered;
            this.updateTextPosition();
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public void setCanLoseFocus(boolean canLoseFocus) {
            this.canLoseFocus = canLoseFocus;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public void setTextColorUneditable(int textColorUneditable) {
            this.textColorUneditable = textColorUneditable;
        }

        public void setHint(Component hint) {
            this.hint = hint.getStyle().equals(Style.EMPTY) ? hint.copy().withStyle(DEFAULT_HINT_STYLE) : hint;
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            this.updateTextPosition();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            this.updateTextPosition();
        }

        @Override
        public void setFocused(boolean focused) {
            if (this.canLoseFocus || focused) {
                this.focused = focused;
                if (focused) {
                    this.focusedTime = Util.getMillis();
                }
            }
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }

        private boolean canConsumeInput() {
            return this.isFocused() && this.editable;
        }

        private int getInnerWidth() {
            return this.width - TEXT_PAD * 2;
        }

        private int getMaxLength() {
            return this.maxLength;
        }

        private void onValueChange(String value) {
            this.onUpdate(value);
            this.updateTextPosition();
        }

        protected abstract void onUpdate(String value);

        private static String sanitizeSingleLine(String value) {
            return value.replace("\r", "").replace("\n", "");
        }

        private void updateTextPosition() {
            this.textX = this.getX() + TEXT_PAD;
            this.textY = this.getY() + (this.height - 8) / 2;
        }

        private void scrollTo(int pos) {
            this.displayPos = Math.min(this.displayPos, this.value.length());
            int innerWidth = this.getInnerWidth();
            String displayed = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), innerWidth);
            int lastPos = displayed.length() + this.displayPos;
            if (pos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, innerWidth, true).length();
            }
            if (pos > lastPos) {
                this.displayPos += pos - lastPos;
            } else if (pos <= this.displayPos) {
                this.displayPos -= this.displayPos - pos;
            }
            this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
        }

        private int getCursorPos(int dir) {
            return Util.offsetByCodepoints(this.value, this.cursorPos, dir);
        }

        private int getWordPosition(int dir) {
            return this.getWordPosition(dir, this.cursorPos, true);
        }

        private int getWordPosition(int dir, int from, boolean stripSpaces) {
            int result = from;
            boolean reverse = dir < 0;
            int abs = Math.abs(dir);
            for (int i = 0; i < abs; i++) {
                if (!reverse) {
                    int length = this.value.length();
                    result = this.value.indexOf(32, result);
                    if (result == -1) {
                        result = length;
                    } else {
                        while (stripSpaces && result < length && this.value.charAt(result) == ' ') {
                            result++;
                        }
                    }
                } else {
                    while (stripSpaces && result > 0 && this.value.charAt(result - 1) == ' ') {
                        result--;
                    }
                    while (result > 0 && this.value.charAt(result - 1) != ' ') {
                        result--;
                    }
                }
            }
            return result;
        }

        public void setCursorPosition(int pos) {
            this.cursorPos = Mth.clamp(pos, 0, this.value.length());
            this.scrollTo(this.cursorPos);
        }

        public void setHighlightPos(int pos) {
            this.highlightPos = Mth.clamp(pos, 0, this.value.length());
            this.scrollTo(this.highlightPos);
        }

        public void moveCursorTo(int pos, boolean extendSelection) {
            this.setCursorPosition(pos);
            if (!extendSelection) {
                this.setHighlightPos(this.cursorPos);
            }
            this.updateTextPosition();
        }

        public void moveCursorToStart(boolean extendSelection) {
            this.moveCursorTo(0, extendSelection);
        }

        public void moveCursorToEnd(boolean extendSelection) {
            this.moveCursorTo(this.value.length(), extendSelection);
        }

        public void moveCursor(int dir, boolean extendSelection) {
            this.moveCursorTo(this.getCursorPos(dir), extendSelection);
        }

        public void insertText(String input) {
            int start = Math.min(this.cursorPos, this.highlightPos);
            int end = Math.max(this.cursorPos, this.highlightPos);
            int maxInsertionLength = this.maxLength - this.value.length() - (start - end);
            if (maxInsertionLength <= 0) {
                return;
            }
            String text = sanitizeSingleLine(StringUtil.filterText(input));
            if (text.length() > maxInsertionLength) {
                if (Character.isHighSurrogate(text.charAt(maxInsertionLength - 1))) {
                    maxInsertionLength--;
                }
                text = text.substring(0, maxInsertionLength);
            }
            if (!this.filter.test(text)) {
                return;
            }
            this.value = new StringBuilder(this.value).replace(start, end, text).toString();
            this.setCursorPosition(start + text.length());
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }

        private void deleteCharsToPos(int pos) {
            if (this.value.isEmpty()) {
                return;
            }
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
                return;
            }
            int start = Math.min(pos, this.cursorPos);
            int end = Math.max(pos, this.cursorPos);
            if (start != end) {
                this.value = new StringBuilder(this.value).delete(start, end).toString();
                this.setCursorPosition(start);
                this.onValueChange(this.value);
                this.moveCursorTo(start, false);
            }
        }

        public void deleteChars(int dir) {
            this.deleteCharsToPos(this.getCursorPos(dir));
        }

        public void deleteWords(int dir) {
            if (!this.value.isEmpty()) {
                if (this.highlightPos != this.cursorPos) {
                    this.insertText("");
                } else {
                    this.deleteCharsToPos(this.getWordPosition(dir));
                }
            }
        }

        public String getHighlighted() {
            int start = Math.min(this.cursorPos, this.highlightPos);
            int end = Math.max(this.cursorPos, this.highlightPos);
            return this.value.substring(start, end);
        }

        private int findClickedPositionInText(MouseButtonEvent event) {
            int positionInText = Math.min(Mth.floor(event.x()) - this.textX, this.getInnerWidth());
            String displayed = this.value.substring(this.displayPos);
            return this.displayPos + this.font.plainSubstrByWidth(displayed, positionInText).length();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            if (event.button() == 1) {
                this.setValue("");
                this.setFocused(true);
                return true;
            }
            if (!this.isValidClickButton(event.buttonInfo())) {
                return false;
            }
            this.setFocused(true);
            this.onClick(event, doubleClick);
            return true;
        }

        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (doubleClick) {
                this.moveCursorToStart(false);
                this.moveCursorToEnd(true);
                return;
            }
            this.moveCursorTo(this.findClickedPositionInText(event), event.hasShiftDown());
        }

        protected void onDrag(MouseButtonEvent event, double dx, double dy) {
            this.moveCursorTo(this.findClickedPositionInText(event), true);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
            if (!this.isFocused() || !this.isValidClickButton(event.buttonInfo()) || !this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            this.onDrag(event, dx, dy);
            return true;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (!this.canConsumeInput()) {
                return false;
            }
            switch (event.key()) {
                case 259 -> {
                    this.deleteText(-1, event.hasControlDownWithQuirk());
                    return true;
                }
                case 261 -> {
                    this.deleteText(1, event.hasControlDownWithQuirk());
                    return true;
                }
                case 262 -> {
                    if (event.hasControlDownWithQuirk()) {
                        this.moveCursorTo(this.getWordPosition(1), event.hasShiftDown());
                    } else {
                        this.moveCursor(1, event.hasShiftDown());
                    }
                    return true;
                }
                case 263 -> {
                    if (event.hasControlDownWithQuirk()) {
                        this.moveCursorTo(this.getWordPosition(-1), event.hasShiftDown());
                    } else {
                        this.moveCursor(-1, event.hasShiftDown());
                    }
                    return true;
                }
                case 268 -> {
                    this.moveCursorToStart(event.hasShiftDown());
                    return true;
                }
                case 269 -> {
                    this.moveCursorToEnd(event.hasShiftDown());
                    return true;
                }
                default -> {
                    if (event.isSelectAll()) {
                        this.moveCursorToEnd(false);
                        this.setHighlightPos(0);
                        return true;
                    }
                    if (event.isCopy()) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        return true;
                    }
                    if (event.isPaste()) {
                        this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                        return true;
                    }
                    if (event.isCut()) {
                        Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                        this.insertText("");
                        return true;
                    }
                    return false;
                }
            }
        }

        @Override
        public boolean charTyped(CharacterEvent event) {
            if (!this.canConsumeInput()) {
                return false;
            }
            if (!event.isAllowedChatCharacter()) {
                return false;
            }
            this.insertText(event.codepointAsString());
            return true;
        }

        private void deleteText(int dir, boolean wholeWord) {
            if (wholeWord) {
                this.deleteWords(dir);
            } else {
                this.deleteChars(dir);
            }
        }

        @Override
        public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), this.backgroundColor);

            if (this.bordered) {
                graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFF505050);
            }

            int color = this.editable ? this.textColor : this.textColorUneditable;
            int relCursorPos = this.cursorPos - this.displayPos;
            String displayed = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean cursorOnScreen = relCursorPos >= 0 && relCursorPos <= displayed.length();
            boolean showCursor = this.isFocused() && TextCursorUtils.isCursorVisible(Util.getMillis() - this.focusedTime) && cursorOnScreen;

            int drawX = this.textX;
            int relHighlightPos = Mth.clamp(this.highlightPos - this.displayPos, 0, displayed.length());

            if (!displayed.isEmpty()) {
                String left = cursorOnScreen ? displayed.substring(0, relCursorPos) : displayed;
                graphics.text(this.font, left, drawX, this.textY, color, this.textShadow);
                drawX += this.font.width(left) + 1;
            }

            boolean insert = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int cursorX = drawX;
            if (!cursorOnScreen) {
                cursorX = relCursorPos > 0 ? this.textX + this.width : this.textX;
            } else if (insert) {
                cursorX = drawX - 1;
                drawX--;
            }

            if (!displayed.isEmpty() && cursorOnScreen && relCursorPos < displayed.length()) {
                graphics.text(this.font, displayed.substring(relCursorPos), drawX, this.textY, color, this.textShadow);
            }

            if (this.hint != null && displayed.isEmpty() && !this.isFocused()) {
                graphics.text(this.font, this.hint, drawX, this.textY, color);
            }

            if (relHighlightPos != relCursorPos) {
                int highlightX = this.textX + this.font.width(displayed.substring(0, relHighlightPos));
                graphics.textHighlight(
                        Math.min(cursorX, this.getX() + this.width),
                        this.textY - 1,
                        Math.min(highlightX - 1, this.getX() + this.width),
                        this.textY + 10,
                        true
                );
            }

            if (showCursor) {
                if (insert) {
                    TextCursorUtils.extractInsertCursor(graphics, cursorX, this.textY, color, 10);
                } else {
                    TextCursorUtils.extractAppendCursor(graphics, this.font, cursorX, this.textY, color, this.textShadow);
                }
            }

            if (this.isMouseOver(mouseX, mouseY)) {
                graphics.requestCursor(this.editable ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
            }
        }
    }

    public abstract static class WithTooltip extends Widget
    {
        protected final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

        public void setTooltip(@Nullable Tooltip tooltip) {
            this.tooltip.set(tooltip);
        }

        public ScreenRectangle getTooltipArea() {
            return this.getRectangle();
        }

        public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            extractContentState(graphics, mouseX, mouseY, a);
            if (!graphics.containsPointInScissor(mouseX, mouseY)) {
                return;
            }
            this.tooltip.refreshTooltipForNextRenderPass(graphics,
                                                         mouseX,
                                                         mouseY,
                                                         this.isHoveredOnTooltipArea(mouseX, mouseY),
                                                         this.isFocused(),
                                                         this.getTooltipArea());
        }

        private boolean isHoveredOnTooltipArea(int mouseX, int mouseY) {
            return this.getTooltipArea().containsPoint(mouseX, mouseY);
        }

        public abstract void extractContentState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);

    }

    private static abstract class ListWidget<E extends ListWidget.Entry> extends Panel implements GuiEventListener, Reloadable
    {
        private static final int SCROLLBAR_WIDTH = 6;
        private static final int SCROLLER_MIN_HEIGHT = 32;
        private static final int COL_SCROLLBAR_TRACK = 0xC0000000;
        private static final int COL_SCROLLBAR_THUMB = 0xFFBBBBBB;

        private final List<E> children = new ArrayList<>();
        private final int entryHeight;
        private double scrollAmount;
        private boolean scrolling;
        private boolean focused;

        public ListWidget(int entryHeight) {
            this.entryHeight = entryHeight;
        }

        @Override
        public void init() {
            for (E child : this.children) {
                child.init();
            }
        }

        @Override
        public ScreenRectangle getRectangle() {
            return super.getRectangle();
        }

        public List<E> entries() {
            return this.children;
        }

        public void clearEntries() {
            this.children.clear();
            this.scrollAmount = 0.0;
        }

        public void addEntry(E entry) {
            this.children.add(entry);
            this.repositionElements();
            this.refreshScrollAmount();
        }

        public void setEntries(List<E> entries) {
            this.children.clear();
            this.children.addAll(entries);
            this.repositionElements();
            this.refreshScrollAmount();
        }

        public double scrollAmount() {
            return this.scrollAmount;
        }

        public void setScrollAmount(double scrollAmount) {
            double max = this.maxScrollAmount();
            this.scrollAmount = Math.clamp(scrollAmount, 0.0, max);
            this.repositionElements();
        }

        public void refreshScrollAmount() {
            this.setScrollAmount(this.scrollAmount);
        }

        public int maxScrollAmount() {
            return Math.max(0, this.contentHeight() - this.getHeight());
        }

        protected int contentHeight() {
            return this.children.size() * this.entryHeight;
        }

        protected boolean scrollable() {
            return this.maxScrollAmount() > 0;
        }

        protected int scrollbarWidth() {
            return SCROLLBAR_WIDTH;
        }

        protected int scrollBarX() {
            return this.getRight() - this.scrollbarWidth();
        }

        protected int scrollerHeight() {
            if (this.contentHeight() <= 0) {
                return this.getHeight();
            }
            int maxThumb = Math.max(1, this.getHeight() - 8);
            int raw = (int) ((float) (this.getHeight() * this.getHeight()) / this.contentHeight());
            return Math.clamp(raw, SCROLLER_MIN_HEIGHT, maxThumb);
        }

        protected int scrollBarY() {
            int max = this.maxScrollAmount();
            if (max == 0) {
                return this.getY();
            }
            return Math.max(this.getY(),
                            (int) this.scrollAmount * (this.getHeight() - this.scrollerHeight()) / max + this.getY());
        }

        protected boolean isOverScrollbar(double x, double y) {
            return x >= this.scrollBarX()
                   && x <= this.scrollBarX() + this.scrollbarWidth()
                   && y >= this.getY()
                   && y < this.getBottom();
        }

        protected boolean updateScrolling(MouseButtonEvent event) {
            this.scrolling = this.scrollable() && this.isValidClickButton(event.buttonInfo()) && this.isOverScrollbar(
                    event.x(),
                    event.y());
            return this.scrolling;
        }

        protected @Nullable E getEntryAtPosition(double x, double y) {
            for (E child : this.children) {
                if (child.isMouseOver(x, y)) {
                    return child;
                }
            }
            return null;
        }

        @Override
        public void repositionElements() {
            int y = this.getY() - (int) this.scrollAmount;
            int rowWidth = Math.max(0, this.getWidth() - this.scrollbarWidth());
            for (E child : this.children) {
                child.setX(this.getX());
                child.setY(y);
                child.setWidth(rowWidth);
                child.setHeight(this.entryHeight);
                child.repositionElements();
                y += this.entryHeight;
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            if (this.updateScrolling(event)) {
                this.playDownSound();
                return true;
            }

            E entry = this.getEntryAtPosition(event.x(), event.y());
            if (entry instanceof Selectable selectable) {
                selectable.select();
                return entry.mouseClicked(event, doubleClick);
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
            if (!this.isMouseOver(mx, my)) {
                return false;
            }
            this.setScrollAmount(this.scrollAmount - scrollY * this.entryHeight);
            return true;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
            if (this.scrolling) {
                if (event.y() < this.getY()) {
                    this.setScrollAmount(0.0);
                }
                else if (event.y() > this.getBottom()) {
                    this.setScrollAmount(this.maxScrollAmount());
                }
                else {
                    double max = Math.max(1, this.maxScrollAmount());
                    int barHeight = this.scrollerHeight();
                    double yDragScale = Math.max(1.0, max / (this.getHeight() - barHeight));
                    this.setScrollAmount(this.scrollAmount + dy * yDragScale);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            this.scrolling = false;
            return false;
        }

        @Override
        public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            extractListBackground(graphics, mouseX, mouseY, a);

            graphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
            for (E child : this.children) {
                if (child.getBottom() >= this.getY() && child.getY() <= this.getBottom()) {
                    child.extractRenderState(graphics, mouseX, mouseY, a);
                }
            }
            graphics.disableScissor();

            int scrollbarX = this.scrollBarX();
            int scrollerHeight = this.scrollerHeight();
            int scrollerY = this.scrollBarY();
            graphics.fill(scrollbarX,
                          this.getY(),
                          scrollbarX + this.scrollbarWidth(),
                          this.getBottom(),
                          COL_SCROLLBAR_TRACK);
            if (this.scrollable()) {
                graphics.fill(scrollbarX,
                              scrollerY,
                              scrollbarX + this.scrollbarWidth(),
                              scrollerY + scrollerHeight,
                              COL_SCROLLBAR_THUMB);
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.getRectangle().containsPoint((int) mouseX, (int) mouseY);
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }

        protected void extractListBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        }

        private static abstract class Entry extends WithTooltip implements Reloadable
        {
            public static final int BG_HOVER = 0x30FFFFFF;

            @Override
            public void repositionElements() {
            }

            @Override
            public void setFocused(boolean focused) {
            }
        }
    }

    private static abstract class Button extends WithTooltip
    {
        protected final Component message;
        protected boolean focused;

        public Button(Component message) {
            this.message = message;
        }

        @Override
        public void extractContentState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            this.extractContents(graphics, mouseX, mouseY, a);
            if (this.focused) {
                graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFFFFFFFF);
            }
        }

        protected abstract void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a);

        @Override
        public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (!this.isValidClickButton(event.buttonInfo()) || !this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            this.onClick(event, doubleClick);
            this.playDownSound();
            return true;
        }

        public abstract void onClick(MouseButtonEvent event, boolean doubleClick);

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }
    }

    private static abstract class SpriteBtn extends Button
    {
        private final Identifier defaultSprite;
        private final Identifier selectedSprite;

        public SpriteBtn(Identifier defaultSprite, Identifier selectedSprite, Component message) {
            super(message);
            this.defaultSprite = defaultSprite;
            this.selectedSprite = selectedSprite;
            this.setWidth(SIDEBAR_ICON_SIZE);
            this.setHeight(SIDEBAR_ICON_SIZE);
            this.setTooltip(Tooltip.create(message));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                                this.isMouseOver(mouseX, mouseY) || this.isFocused()
                                ? this.selectedSprite
                                : this.defaultSprite,
                                this.getX(),
                                this.getY(),
                                this.getWidth(),
                                this.getHeight());
        }
    }

    private static abstract class ColorBtn extends Button
    {
        private final Font font;
        private final int defaultColor;
        private final int hoveredColor;

        public ColorBtn(Font font, int defaultColor, int hoveredColor, Component message) {
            super(message);
            this.font = font;
            this.defaultColor = defaultColor;
            this.hoveredColor = hoveredColor;
            this.setWidth(DETAIL_BUTTON_WIDTH);
            this.setHeight(ENTRY_HEIGHT);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(),
                          this.getY(),
                          this.getRight(),
                          this.getBottom(),
                          this.isMouseOver(mouseX, mouseY) ? this.hoveredColor : this.defaultColor);
            graphics.centeredText(this.font,
                                  this.message,
                                  this.getX() + this.getWidth() / 2,
                                  this.getY() + (this.getHeight() - 8) / 2,
                                  0xFFFFFFFF);
        }
    }
}

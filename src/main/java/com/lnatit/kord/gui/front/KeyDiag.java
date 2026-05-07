package com.lnatit.kord.gui.front;

import com.lnatit.kord.Kord;
import com.lnatit.kord.gui.Backend;
import com.lnatit.kord.result.ConflictResult;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
    public static final int TOTAL_WIDTH = 9;
    public static final int BINDINGLIST_WIDTH_RATIO = 5;
    public static final int TOOLBAR_HEIGHT = 24;
    public static final int TEXT_PAD = 4;


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
        this.addRenderableWidget(this.sidebar.exitButton);

        this.bindingList.init();
        this.addRenderableOnly(this.bindingList);
        this.addRenderableWidget(this.bindingList.searchBar);
        this.addRenderableWidget(this.bindingList.bindings);

        this.diagnostics.init();
        this.addRenderableOnly(this.diagnostics);
        this.addRenderableOnly(this.diagnostics.filter);
        this.addRenderableWidget(this.diagnostics.conflicts);
        this.addRenderableOnly(this.diagnostics.detail);
        this.addRenderableWidget(this.diagnostics.detail.navigateButton);
        this.addRenderableWidget(this.diagnostics.detail.editButton);

        this.repositionElements();
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
        return ((this.width - this.sidebarWidth() - PADDING) * BINDINGLIST_WIDTH_RATIO / TOTAL_WIDTH) - PADDING * 2;
    }

    private int diagnosticsX() {
        return this.bindingListX() + this.bindingListWidth() + PADDING;
    }

    private int diagnosticsWidth() {
        return this.width - this.diagnosticsX() - PADDING;
    }

    private class Sidebar extends Panel
    {
        public static final int BUTTON_WIDTH = SIDEBAR_ICON_SIZE;
        public static final int BUTTON_HEIGHT = SIDEBAR_ICON_SIZE;

        public static final int BACKGROUND_COLOR = 0x0F808080;

        // TODO
        public static final Identifier EXIT_DEFAULT = Kord.id("textures/gui/exit_default.png");
        public static final Identifier EXIT_SELECTED = Kord.id("textures/gui/exit_selected.png");
        public static final Identifier REFRESH_DEFAULT = Kord.id("textures/gui/refresh_default.png");
        public static final Identifier REFRESH_SELECTED = Kord.id("textures/gui/refresh_selected.png");
        public static final Identifier RELOAD_DEFAULT = Kord.id("textures/gui/reload_default.png");
        public static final Identifier RELOAD_SELECTED = Kord.id("textures/gui/reload_selected.png");
        public static final Identifier SETTINGS_DEFAULT = Kord.id("textures/gui/settings_default.png");
        public static final Identifier SETTINGS_SELECTED = Kord.id("textures/gui/settings_selected.png");

        public static final Component EXIT_MSG = Component.translatable("gui.kord.exit");
        public static final Component SETTINGS_MSG = Component.translatable("gui.kord.settings");
        public static final Component RELOAD_MSG = Component.translatable("gui.kord.reload");
        public static final Component REFRESH_MSG = Component.translatable("gui.kord.refresh");

        private final SpriteBtn exitButton = new SpriteBtn(0,
                                                           0,
                                                           BUTTON_WIDTH,
                                                           BUTTON_HEIGHT,
                                                           EXIT_DEFAULT,
                                                           EXIT_SELECTED,
                                                           EXIT_MSG,
                                                           b -> Minecraft.getInstance().setScreen(previousScreen));
        private final SpriteBtn settingsButton = new SpriteBtn(0,
                                                               0,
                                                               BUTTON_WIDTH,
                                                               BUTTON_HEIGHT,
                                                               SETTINGS_DEFAULT,
                                                               SETTINGS_SELECTED,
                                                               SETTINGS_MSG,
                                                               b -> {}

        );
        private final SpriteBtn reloadButton =
                new SpriteBtn(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, RELOAD_DEFAULT, RELOAD_SELECTED, RELOAD_MSG, b -> {});
        private final SpriteBtn refreshButton = new SpriteBtn(0,
                                                              0,
                                                              BUTTON_WIDTH,
                                                              BUTTON_HEIGHT,
                                                              REFRESH_DEFAULT,
                                                              REFRESH_SELECTED,
                                                              REFRESH_MSG,
                                                              b -> {});

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

    private class BindingList extends Panel
    {
        public static final int BACKGROUND_COLOR = 0x0F202020;

        private final Bindings bindings = new Bindings();
        private final SearchBar searchBar = new SearchBar();

        @Override
        public void init() {
            this.bindings.init();
            this.searchBar.init();
        }

        @Override
        public void repositionElements() {
            this.setX(KeyDiag.this.bindingListX());
            this.setY(0);
            this.setWidth(KeyDiag.this.bindingListWidth());
            this.setHeight(KeyDiag.this.height);

            int listHeight = this.getHeight() - TOOLBAR_HEIGHT;
            this.bindings.setRectangle(this.getWidth(), listHeight, this.getX(), this.getY());
            this.bindings.repositionElements();

            this.searchBar.setRectangle(this.getWidth(), TOOLBAR_HEIGHT, this.getX(), this.bindings.getBottom());
            this.searchBar.repositionElements();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_COLOR);
        }
        // Bindings
        // SearchBar

        private class Bindings extends ListWidget<Bindings.KeyEntry>
        {
            @Override
            public void init() {
                for (KeyMapping key : Minecraft.getInstance().options.keyMappings) {
                    this.addEntry(new KeyEntry(key));
                }
                super.init();
            }

            private final class CategoryEntry extends ListWidget.Entry
            {
                public static final int CATEGORY_COLOR = 0xFFDDDDDD;

                private final KeyMapping.Category category;

                private CategoryEntry(KeyMapping.Category category) {this.category = category;}

                @Override
                public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    graphics.centeredText(KeyDiag.this.font,
                                          category.label(),
                                          this.getX() + this.getWidth() / 2,
                                          this.getY() + (this.getHeight() - 9) / 2,
                                          CATEGORY_COLOR);
                }

            }

            private final class KeyEntry extends ListWidget.Entry implements Selectable
            {
                private final KeyMapping keyMapping;
                private Severity severity = Severity.SAFE;

                public KeyEntry(KeyMapping keyMapping) {
                    super();
                    this.keyMapping = keyMapping;
                    this.refreshSeverity();
                }

                @Override
                public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    int rowX = this.getX();
                    int rowY = this.getY();
                    int rowW = this.getWidth();
                    int rowH = this.getHeight();

                    int bindBtnX = rowX + rowW / 2;
                    int bindBtnW = rowW - rowW / 2;

                    boolean hovered = this.isMouseOver(mouseX, mouseY);
                    if (hovered) {
                        graphics.fill(rowX, rowY, rowX + rowW, rowY + rowH, BG_HOVER);
                    }

                    int tint = severityTint(this.severity);
                    if (tint != 0) {
                        graphics.fill(bindBtnX, rowY, rowX + rowW, rowY + rowH, tint);
                    }

                    if (this.isFocused()) {
                        graphics.outline(rowX, rowY, rowW, rowH, 0xFFFFFFFF);
                    }

                    int textY = rowY + (rowH - 9) / 2;
                    KeyDiag.drawScrollingText(
                            graphics,
                            KeyDiag.this.font,
                            this.keyMapping.getDisplayName(),
                            rowX + TEXT_PAD,
                            rowY,
                            bindBtnX - TEXT_PAD,
                            rowY + rowH,
                            0xFFEEEEEE,
                            hovered
                    );
                    graphics.centeredText(KeyDiag.this.font,
                                          this.keyMapping.getTranslatedKeyMessage(),
                                          bindBtnX + bindBtnW / 2,
                                          textY,
                                          0xFFFFFFFF);
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

        private class SearchBar extends Widget
        {
            public SearchBar() {
                super(0, 0, 0, 0, Component.empty());
            }

            @Override
            public void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }
        }
    }

    private class Diagnostics extends Panel
    {
        public static final int TOTAL_HEIGHT = 2;
        public static final int LIST_HIGHT_RATIO = 1;

        public static final int BACKGROUND_COLOR = 0x0F202020;

        private final Filter filter = new BySeverity();
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

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        }

        private abstract class Filter extends Panel
        {
            @Override
            public void repositionElements() {
                this.setX(Diagnostics.this.getX());
                this.setY(Diagnostics.this.getY());
                this.setWidth(Diagnostics.this.getWidth());
                this.setHeight(TOOLBAR_HEIGHT);
            }

            // ToggleBtn ...
        }

        private class BySeverity extends Filter
        {
            @Override
            public void repositionElements() {
                super.repositionElements();
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }

            @Override
            public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {

            }
            // INFO WARNING SEVERE
        }

        private class ByKey extends Filter
        {
            @Override
            public void repositionElements() {
                super.repositionElements();
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }

            @Override
            public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {

            }
            // SearchBar
        }

        private class Conflicts extends ListWidget<Conflicts.Entry>
        {
            @Override
            public void init() {
                for (ConflictResult result : Backend.filter()) {
                    this.addEntry(new Entry(result));
                }
                super.init();
            }


            private final class Entry extends ListWidget.Entry implements Selectable
            {
                private final ConflictResult result;

                public Entry(ConflictResult result) {
                    super();
                    this.result = result;
                }

                @Override
                public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

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

        private class Detail extends Panel
        {
            public static final int BUTTON_WIDTH = 48;
            public static final int BUTTON_DEFAULT_COLOR = 0x0F202020;
            public static final int BUTTON_SELECTED_COLOR = 0xFF404040;

            private final ColorBtn navigateButton = new ColorBtn(0,
                                                                 0,
                                                                 BUTTON_WIDTH,
                                                                 TOOLBAR_HEIGHT,
                                                                 BUTTON_DEFAULT_COLOR,
                                                                 BUTTON_SELECTED_COLOR,
                                                                 Component.translatable("gui.kord.navigate"),
                                                                 b -> {
                                                                 });
            private final ColorBtn editButton = new ColorBtn(0,
                                                             0,
                                                             BUTTON_WIDTH,
                                                             TOOLBAR_HEIGHT,
                                                             BUTTON_DEFAULT_COLOR,
                                                             BUTTON_SELECTED_COLOR,
                                                             Component.translatable("gui.kord.edit"),
                                                             b -> {
                                                             });

            @Override
            public void repositionElements() {
                int detailY = Diagnostics.this.conflicts.getBottom() + PADDING;
                int detailHeight = Diagnostics.this.getBottom() - detailY;
                this.setX(Diagnostics.this.getX());
                this.setY(detailY);
                this.setWidth(Diagnostics.this.getWidth());
                this.setHeight(detailHeight);

                int buttonX = this.getRight() - BUTTON_WIDTH - PADDING;
                this.navigateButton.setRectangle(BUTTON_WIDTH, TOOLBAR_HEIGHT, buttonX, this.getY());
                this.editButton.setRectangle(BUTTON_WIDTH,
                                             TOOLBAR_HEIGHT,
                                             buttonX,
                                             this.getY() + TOOLBAR_HEIGHT + PADDING);
            }

            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

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
            net.minecraft.client.gui.Font font,
            Component text,
            int x1, int y1, int x2, int y2,
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

    private interface Reloadable
    {
        void repositionElements();

        default void init() {}
    }

    private sealed interface Selectable permits BindingList.Bindings.KeyEntry, Diagnostics.Conflicts.Entry {
        void select();
    }

    private static abstract class Panel implements Reloadable, LayoutElement, Renderable
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
        public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
        }
    }

    private static abstract class Widget extends AbstractWidget implements Reloadable
    {
        public Widget(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static abstract class ListWidget<E extends ListWidget.Entry> extends Widget
    {
        private static final int SCROLLBAR_WIDTH = 6;
        private static final int SCROLLER_MIN_HEIGHT = 32;
        private static final int COL_SCROLLBAR_TRACK = 0x40202020;
        private static final int COL_SCROLLBAR_THUMB = 0xA0AAAAAA;

        private List<E> children = new ArrayList<>();
        private int entryHeight = 20;
        private double scrollAmount;
        private boolean scrolling;

        public ListWidget() {
            super(0, 0, 0, 0, Component.empty());
        }

        public void setEntryHeight(int entryHeight) {
            this.entryHeight = Math.max(1, entryHeight);
            this.refreshScrollAmount();
            this.repositionElements();
        }

        public int entryHeight() {
            return this.entryHeight;
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

        @Override
        public void init() {
            for (E child : this.children) {
                child.init();
            }
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
                if (child.getRectangle().containsPoint((int) x, (int) y)) {
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
            if (!this.visible || !this.isMouseOver(event.x(), event.y())) {
                return false;
            }
            if (this.updateScrolling(event)) {
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
            if (!this.visible || !this.isMouseOver(mx, my)) {
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
            return super.mouseDragged(event, dx, dy);
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            this.scrolling = false;
            super.onRelease(event);
        }

        @Override
        protected final void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
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
            if (this.scrollable()) {
                graphics.fill(scrollbarX,
                              this.getY(),
                              scrollbarX + this.scrollbarWidth(),
                              this.getBottom(),
                              COL_SCROLLBAR_TRACK);
                graphics.fill(scrollbarX,
                              scrollerY,
                              scrollbarX + this.scrollbarWidth(),
                              scrollerY + scrollerHeight,
                              COL_SCROLLBAR_THUMB);
            }
        }

        protected void extractListBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        }

        private static abstract class Entry extends Panel implements GuiEventListener
        {
            public static final int BG_HOVER = 0x30FFFFFF;

            @Override
            public void repositionElements() {
            }

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

            @Override
            public void setFocused(boolean focused) {
            }

            @Override
            public boolean isFocused() {
                return false;
            }
        }
    }

    // TODO don't use button
    private static abstract class Btn extends Button
    {
        public Btn(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }
    }

    private static class SpriteBtn extends Btn
    {
        private final Identifier defaultSprite;
        private final Identifier selectedSprite;

        protected SpriteBtn(
                int x,
                int y,
                int width,
                int height,
                Identifier defaultSprite,
                Identifier selectedSprite,
                Component message,
                OnPress onPress
        ) {
            super(x, y, width, height, message, onPress);
            this.defaultSprite = defaultSprite;
            this.selectedSprite = selectedSprite;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                                this.isHoveredOrFocused() ? this.selectedSprite : this.defaultSprite,
                                this.getX(),
                                this.getY(),
                                this.getWidth(),
                                this.getHeight());
        }
    }

    private static class ColorBtn extends Btn
    {
        private final int defaultColor;
        private final int selectedColor;

        public ColorBtn(
                int x,
                int y,
                int width,
                int height,
                int defaultColor,
                int selectedColor,
                Component message,
                OnPress onPress
        ) {
            super(x, y, width, height, message, onPress);
            this.defaultColor = defaultColor;
            this.selectedColor = selectedColor;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(),
                          this.getY(),
                          this.getRight(),
                          this.getBottom(),
                          this.isHoveredOrFocused() ? this.selectedColor : this.defaultColor);
        }
    }
}

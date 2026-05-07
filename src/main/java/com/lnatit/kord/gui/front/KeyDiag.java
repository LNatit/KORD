package com.lnatit.kord.gui.front;

import com.lnatit.kord.Kord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class KeyDiag extends Screen {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;

    public static final int PADDING = 6;
    public static final int SIDEBAR_ICON_SIZE = 24;
    public static final int TOTAL_WIDTH = 9;
    public static final int BINDINGLIST_WIDTH_RATIO = 5;
    public static final int TOOLBAR_HEIGHT = 24;

    // default settings
    public static final int SIDEBAR_X = PADDING;
    public static final int SIDEBAR_Y = 0;
    public static final int SIDEBAR_WIDTH = 24;
    public static final int SIDEBAR_HEIGHT = HEIGHT;

    public static final int BINDINGLIST_X = SIDEBAR_X + SIDEBAR_WIDTH + PADDING;
    public static final int BINDINGLIST_Y = 0;
    public static final int BINDINGLIST_WIDTH = ((WIDTH - SIDEBAR_WIDTH - PADDING) * BINDINGLIST_WIDTH_RATIO / TOTAL_WIDTH) - PADDING * 2;
    public static final int BINDINGLIST_HEIGHT = HEIGHT;

    public static final int DIAGNOSTICS_X = BINDINGLIST_X + BINDINGLIST_WIDTH + PADDING;
    public static final int DIAGNOSTICS_Y = 0;
    public static final int DIAGNOSTICS_WIDTH = WIDTH - DIAGNOSTICS_X - PADDING;
    public static final int DIAGNOSTICS_HEIGHT = HEIGHT;


    private final Screen previousScreen;

    private final Sidebar sidebar = new Sidebar();
    private final BindingList bindingList = new BindingList();
    private final Diagnostics diagnostics = new Diagnostics();

    private int selectedBinding = -1;
    private int selectedDiagnos = -1;


    public KeyDiag(Screen previous) {
        super(Component.empty());
        this.previousScreen = previous;
    }

    @Override
    protected void init() {
        // init sidebar
        this.sidebar.init();


        this.bindingList.init();


        this.diagnostics.init();


        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        // init yourself first

        // then reposition children
        this.sidebar.repositionElements();
        this.bindingList.repositionElements();
        this.diagnostics.repositionElements();
    }

    private class Sidebar extends Widget {
        public static final int BUTTON_WIDTH = SIDEBAR_ICON_SIZE;
        public static final int BUTTON_HEIGHT = SIDEBAR_ICON_SIZE;

        public static final int BACKGROUND_COLOR = 0x0F808080;

        // TODO
        public static final Identifier EXIT_DEFAULT = Kord.id("textures/gui/exit_default.png");
        public static final Identifier EXIT_SELECTED = Kord.id("textures/gui/exit_selected.png");

        public static final Component EXIT_MSG = Component.translatable("gui.kord.exit");

        private final SpriteBtn exitButton = new SpriteBtn(getX(), getY(), BUTTON_WIDTH, BUTTON_HEIGHT, EXIT_DEFAULT, EXIT_SELECTED, EXIT_MSG, b -> Minecraft.getInstance().setScreen(previousScreen));
        // TODO
//        private final SidebarButton refreshButton;
//        private final SidebarButton reloadButton;
//        private final SidebarButton settingsButton;


        public Sidebar() {
            super(SIDEBAR_X, SIDEBAR_Y, SIDEBAR_WIDTH, SIDEBAR_HEIGHT, Component.empty());
        }


        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_COLOR);
            this.exitButton.extractContents(graphics, mouseX, mouseY, a);
        }

        @Override
        protected void init() {

        }

        @Override
        protected void repositionElements() {

        }
    }

    private class BindingList extends Widget {
        public static final int LIST_X = BINDINGLIST_X;
        public static final int LIST_Y = BINDINGLIST_Y;
        public static final int LIST_WIDTH = BINDINGLIST_WIDTH;
        public static final int LIST_HEIGHT = BINDINGLIST_HEIGHT - TOOLBAR_HEIGHT;

        public static final int SEARCHBAR_X = BINDINGLIST_X;
        public static final int SEARCHBAR_Y = LIST_Y + LIST_HEIGHT;
        public static final int SEARCHBAR_WIDTH = BINDINGLIST_WIDTH;
        public static final int SEARCHBAR_HEIGHT = TOOLBAR_HEIGHT;

        public static final int BACKGROUND_COLOR = 0x0F202020;

        private final List list = new List();
        private final SearchBar searchBar = new SearchBar();

        public BindingList() {
            super(BINDINGLIST_X, BINDINGLIST_Y, BINDINGLIST_WIDTH, BINDINGLIST_HEIGHT, Component.empty());
        }

        @Override
        protected void init() {

        }

        @Override
        protected void repositionElements() {

        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), BACKGROUND_COLOR);
            this.list.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            this.searchBar.extractRenderState(graphics, mouseX, mouseY, a);
        }
        // List
        // SearchBar

        private class List extends Widget {
            public List() {
                super(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, Component.empty());
            }

            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }

            private class Entry {
            }
        }

        private class SearchBar extends Widget {
            public SearchBar() {
                super(SEARCHBAR_X, SEARCHBAR_Y, SEARCHBAR_WIDTH, SEARCHBAR_HEIGHT, Component.empty());
            }

            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }
        }
    }

    private class Diagnostics extends Widget {
        public static final int TOTAL_HEIGHT = 2;
        public static final int LIST_HIGHT_RATIO = 1;

        public static final int FILTER_X = DIAGNOSTICS_X;
        public static final int FILTER_Y = DIAGNOSTICS_Y;
        public static final int FILTER_WIDTH = DIAGNOSTICS_WIDTH;
        public static final int FILTER_HEIGHT = TOOLBAR_HEIGHT;

        public static final int LIST_X = DIAGNOSTICS_X;
        public static final int LIST_Y = FILTER_Y + FILTER_HEIGHT;
        public static final int LIST_WIDTH = DIAGNOSTICS_WIDTH;
        public static final int LIST_HEIGHT = (DIAGNOSTICS_HEIGHT - FILTER_HEIGHT - PADDING) * LIST_HIGHT_RATIO / TOTAL_HEIGHT;

        public static final int DETAIL_X = DIAGNOSTICS_X;
        public static final int DETAIL_Y = LIST_Y + LIST_HEIGHT + PADDING;
        public static final int DETAIL_WIDTH = DIAGNOSTICS_WIDTH;
        public static final int DETAIL_HEIGHT = DIAGNOSTICS_HEIGHT - DETAIL_Y;

        public static final int BACKGROUND_COLOR = 0x0F202020;

        private final List list = new List();
        private final Detail detail = new Detail();

        public Diagnostics() {
            super(DIAGNOSTICS_X, DIAGNOSTICS_Y, DIAGNOSTICS_WIDTH, DIAGNOSTICS_HEIGHT, Component.empty());
        }

        @Override
        protected void init() {

        }

        @Override
        protected void repositionElements() {

        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        }

        private abstract class Filter extends Widget {
            public Filter() {
                super(FILTER_X, FILTER_Y, FILTER_WIDTH, FILTER_HEIGHT, Component.empty());
            }
            // ToggleBtn ...
        }

        private class BySeverity extends Filter {
            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }
            // INFO WARNING SEVERE
        }

        private class ByKey extends Filter {
            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }
            // SearchBar
        }

        private class List extends Widget {
            public List() {
                super(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, Component.empty());
            }

            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }
        }

        private class Detail extends Widget {
            public static final int BUTTON_WIDTH = 48;
            public static final int BUTTON_DEFAULT_COLOR = 0x0F202020;
            public static final int BUTTON_SELECTED_COLOR = 0xFF404040;

            public static final int NAVBTN_X = DETAIL_X + DETAIL_WIDTH - BUTTON_WIDTH - PADDING;
            public static final int NAVBTN_Y = DETAIL_Y;
            public static final int NAVBTN_WIDTH = BUTTON_WIDTH;
            public static final int NAVBTN_HEIGHT = TOOLBAR_HEIGHT;

            public static final int EDITBTN_X = NAVBTN_X;
            public static final int EDITBTN_Y = NAVBTN_Y + NAVBTN_HEIGHT + PADDING;
            public static final int EDITBTN_WIDTH = BUTTON_WIDTH;
            public static final int EDITBTN_HEIGHT = TOOLBAR_HEIGHT;

            private final ColorBtn navigateButton = new ColorBtn(NAVBTN_X, NAVBTN_Y, NAVBTN_WIDTH, NAVBTN_HEIGHT, BUTTON_DEFAULT_COLOR, BUTTON_SELECTED_COLOR, Component.translatable("gui.kord.navigate"), b -> {
            });
            private final ColorBtn editButton = new ColorBtn(EDITBTN_X, EDITBTN_Y, EDITBTN_WIDTH, EDITBTN_HEIGHT, BUTTON_DEFAULT_COLOR, BUTTON_SELECTED_COLOR, Component.translatable("gui.kord.edit"), b -> {
            });

            public Detail() {
                super(DETAIL_X, DETAIL_Y, DETAIL_WIDTH, DETAIL_HEIGHT, Component.empty());
            }

            @Override
            protected void init() {

            }

            @Override
            protected void repositionElements() {

            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

            }

            // Message  NavigateBtn
            //          EditBtn
        }
    }

    private static abstract class Panel implements LayoutElement, Renderable {
        private int x;
        private int y;
        private int width;
        private int height;

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
    }

    private static abstract class Widget extends AbstractWidget {
        public Widget(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        protected abstract void init();

        protected abstract void repositionElements();
    }

    // TODO don't use button
    private static abstract class Btn extends Button {
        public Btn(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }
    }

    private static class SpriteBtn extends Btn {
        private final Identifier defaultSprite;
        private final Identifier selectedSprite;

        protected SpriteBtn(int x, int y, int width, int height, Identifier defaultSprite, Identifier selectedSprite, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress);
            this.defaultSprite = defaultSprite;
            this.selectedSprite = selectedSprite;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.isHoveredOrFocused() ? this.selectedSprite : this.defaultSprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    private static class ColorBtn extends Btn {
        private final int defaultColor;
        private final int selectedColor;

        public ColorBtn(int x, int y, int width, int height, int defaultColor, int selectedColor, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress);
            this.defaultColor = defaultColor;
            this.selectedColor = selectedColor;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), this.isHoveredOrFocused() ? this.selectedColor : this.defaultColor);
        }
    }
}

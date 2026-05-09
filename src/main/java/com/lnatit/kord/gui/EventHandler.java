package com.lnatit.kord.gui;

import com.lnatit.kord.Kord;
import com.lnatit.kord.gui.front.KeyDiag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = Kord.MOD_ID)
public class EventHandler
{
    public static final int EXTRA_PADDING_Y = 2;
    public static final int OUTER_PADDING = 9;
    public static final int EXTRA_PADDING_X = 5;
    public static final int BUTTON_SIZE = 20;

    @SubscribeEvent
    public static void onControlsScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ControlsScreen screen && screen.list != null) {
            OptionsList.AbstractEntry old = screen.list.children.getFirst();
            screen.list.children.set(0, new WrappedEntry((OptionsList.Entry) old));
        }
    }

    private static class WrappedEntry extends OptionsList.Entry
    {
        private final Button button;

        public WrappedEntry(OptionsList.Entry original) {
            super(original.children, original.screen);
            this.button =
                    Button.builder(Component.literal("K"), b -> Minecraft.getInstance().setScreen(new KeyDiag(this.screen)))
                          .size(BUTTON_SIZE, BUTTON_SIZE)
                          .build();
            this.setX(original.getX());
            this.setY(original.getY());
            this.setWidth(original.getWidth());
            this.setHeight(original.getHeight());
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            AbstractWidget last = this.children.getLast().widget();
            last.setWidth(last.getWidth() - EXTRA_PADDING_X - BUTTON_SIZE);
            this.button.setY(this.getY() + EXTRA_PADDING_Y);
            this.button.setX(this.getX() + width - BUTTON_SIZE);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            List<GuiEventListener> result = new ArrayList<>(super.children());
            result.add(this.button);
            return result;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            super.extractContent(graphics, mouseX, mouseY, hovered, a);
            this.button.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }
}

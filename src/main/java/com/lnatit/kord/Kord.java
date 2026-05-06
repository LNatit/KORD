package com.lnatit.kord;

import com.lnatit.kord.data.context.ContextReloadListener;
import com.lnatit.kord.data.mutex.MutexSetManager;
import com.lnatit.kord.data.override.DatapackOverrideReloader;
import com.lnatit.kord.data.resource.ResourceReloadListener;
import com.lnatit.kord.data.semantic.KeySemanticManager;
import com.lnatit.kord.gui.KeyBindingScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Kord.MOD_ID, dist = Dist.CLIENT)
public class Kord
{
    public static final String MOD_ID = "kord";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Kord(IEventBus modEventBus, ModContainer modContainer) {
        // TODO test code
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (c, s) -> new KeyBindingScreen());
        modEventBus.addListener(this::registerKordReloadListeners);
    }

    private void registerKordReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(id("contexts"), ContextReloadListener.INSTANCE);
        event.addListener(id("mutex_sets"), MutexSetManager.INSTANCE);
        event.addListener(id("resources"), ResourceReloadListener.INSTANCE);
        event.addListener(id("key_semantics"), KeySemanticManager.INSTANCE);
        event.addListener(id("overrides"), DatapackOverrideReloader.INSTANCE);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}

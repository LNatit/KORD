package com.lnatit.chord;

import com.lnatit.chord.data.context.ContextReloadListener;
import com.lnatit.chord.data.mutex.MutexSetManager;
import com.lnatit.chord.data.override.DatapackOverrideReloader;
import com.lnatit.chord.data.resource.ResourceReloadListener;
import com.lnatit.chord.data.semantic.KeySemanticManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Chord.MOD_ID, dist = Dist.CLIENT)
public class Chord {
    public static final String MOD_ID = "chord";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Chord(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerChordReloadListeners);
    }



    private void registerChordReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ContextReloadListener.INSTANCE);
        event.registerReloadListener(MutexSetManager.INSTANCE);
        event.registerReloadListener(ResourceReloadListener.INSTANCE);
        event.registerReloadListener(KeySemanticManager.INSTANCE);
        event.registerReloadListener(DatapackOverrideReloader.INSTANCE);
    }
}

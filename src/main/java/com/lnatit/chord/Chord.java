package com.lnatit.chord;

import com.lnatit.chord.resource.KeySemanticManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Chord {
    public static final String MOD_ID = "chord";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Chord(IEventBus modEventBus, ModContainer modContainer) {

    }



    private void registerSemanticReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(KeySemanticManager.INSTANCE);
    }
}

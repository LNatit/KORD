package com.lnatit.chord.data.semantic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class KeySemanticManager extends SimpleJsonResourceReloadListener {
    public static final KeySemanticManager INSTANCE = new KeySemanticManager();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private KeySemanticManager() {
        super(GSON, "key_semantics");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }

    private static KeyMapping lookup(String name) {
        return KeyMapping.ALL.get(name);
    }
}

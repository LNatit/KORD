package com.lnatit.chord.data.context;

import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.semantic.KeyContext;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ContextReloadListener extends SimpleJsonResourceReloadListener {
    public static final ContextReloadListener INSTANCE = new ContextReloadListener();

    private ContextReloadListener() {
        super(Codecs.GSON, "contexts");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_contexts");
        KeyContext.init();

        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            DataResult<ContextDefinition> result = decodeDefinition(json);
            if (result.isError()) {
                Chord.LOGGER.warn("Failed to parse context definition in '{}': {}", id, result.error().orElseThrow());
                continue;
            }
            ContextDefinition definition = result.getOrThrow();

            if (definition.isInvalid()) {
                Chord.LOGGER.debug("Context definition in '{}' is invalid and will be ignored.", id);
                continue;
            }

            try {
                KeyContext existing = KeyContext.of(definition.id());
                if (existing != null) {
                    Chord.LOGGER.warn("Context id '{}' from '{}' overrides an existing context.", definition.id(), id);
                }
                KeyContext.register(definition.toKeyContext());
                loaded++;
            } catch (Exception e) {
                Chord.LOGGER.warn("Failed to register context '{}' from '{}': {}", definition.id(), id, e.getMessage());
            }
        }

        Chord.LOGGER.info("Loaded {} context definitions.", loaded);
        profiler.pop();
    }

    private static DataResult<ContextDefinition> decodeDefinition(JsonElement json) {
        // Placeholder: ContextDefinition codec wiring is intentionally deferred.
        if (json.isJsonNull()) {
            return DataResult.error(() -> "ContextDefinition json is null.");
        }
        return DataResult.error(() -> "ContextDefinition codec is not wired yet.");
    }
}

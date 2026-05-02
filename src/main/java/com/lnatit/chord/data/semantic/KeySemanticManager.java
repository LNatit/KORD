package com.lnatit.chord.data.semantic;

import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.eval.intent.Intent;
import com.lnatit.chord.semantic.SemanticalKey;
import com.mojang.serialization.DataResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class KeySemanticManager extends SimpleJsonResourceReloadListener {
    public static final KeySemanticManager INSTANCE = new KeySemanticManager();

    private KeySemanticManager() {
        super(Codecs.GSON, "key_semantics");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_key_semantics");

        // Reset all keys to their mixin-provided fallback before datapack application.
        KeyMapping.ALL.values().forEach(key -> ((SemanticalKey) key).chord$resetSemantic());
        int loaded = 0;
        Intent.beginDecode();
        try {
            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                ResourceLocation id = entry.getKey();
                JsonElement json = entry.getValue();

                DataResult<KeyDefinition> result = decodeDefinition(json);
                if (result.isError()) {
                    Chord.LOGGER.warn("Failed to parse key definition in '{}': {}", id, result.error().orElseThrow());
                    continue;
                }
                KeyDefinition definition = result.getOrThrow();

                Chord.LOGGER.debug("Inspecting key definition in '{}'...", id);
                if (!definition.checkValid()) {
                    Chord.LOGGER.info("Key definition in '{}' is invalid and will be ignored.", id);
                    continue;
                }

                KeyMapping key = SemanticalKey.lookup(definition.name());
                if (key == null) {
                    Chord.LOGGER.warn("Key '{}' not found for key semantics '{}', ignored.", definition.name(), id);
                    continue;
                }

                try {
                    ((SemanticalKey) key).chord$setSemantic(definition.semantic().toSemantic());
                    loaded++;
                } catch (Exception e) {
                    Chord.LOGGER.warn("Failed to apply key semantic for '{}' in '{}': {}", definition.name(), id, e.getMessage());
                }
            }
        } finally {
            Intent.endDecode();
        }

        Chord.LOGGER.info("Loaded {} key semantic definitions.", loaded);
        profiler.pop();
    }

    private static DataResult<KeyDefinition> decodeDefinition(JsonElement json) {
        // Placeholder: KeyDefinition codec is intentionally deferred until data schema stabilizes.
        if (json.isJsonNull()) {
            return DataResult.error(() -> "KeyDefinition json is null.");
        }
        return DataResult.error(() -> "KeyDefinition codec is not wired yet.");
    }

}

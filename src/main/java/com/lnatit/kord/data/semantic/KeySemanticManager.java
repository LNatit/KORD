package com.lnatit.kord.data.semantic;

import com.lnatit.kord.Kord;
import com.lnatit.kord.data.Codecs;
import com.lnatit.kord.eval.intent.Intent;
import com.lnatit.kord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class KeySemanticManager extends SimpleJsonResourceReloadListener<KeyDefinition> {
    public static final KeySemanticManager INSTANCE = new KeySemanticManager();

    private KeySemanticManager() {
        super(Codecs.KEY_DEFINITION_CODEC, FileToIdConverter.json("key_semantics"));
    }

    @Override
    protected void apply(Map<Identifier, KeyDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("kord_key_semantics");

        // Reset all keys to their mixin-provided fallback before datapack application.
        KeyMapping.ALL.values().forEach(key -> ((SemanticalKey) key).kord$resetSemantic());
        int loaded = 0;
        Intent.beginDecode();
        try {
            for (Map.Entry<Identifier, KeyDefinition> entry : map.entrySet()) {
                Identifier id = entry.getKey();
                KeyDefinition definition = entry.getValue();

                Kord.LOGGER.debug("Inspecting key definition in '{}'...", id);
                if (!definition.checkValid()) {
                    Kord.LOGGER.info("Key definition in '{}' is invalid and will be ignored.", id);
                    continue;
                }

                KeyMapping key = SemanticalKey.lookup(definition.name());
                if (key == null) {
                    Kord.LOGGER.warn("Key '{}' not found for key semantics '{}', ignored.", definition.name(), id);
                    continue;
                }

                try {
                    ((SemanticalKey) key).kord$setSemantic(definition.semantic().toSemantic());
                    loaded++;
                } catch (Exception e) {
                    Kord.LOGGER.warn("Failed to apply key semantic for '{}' in '{}': {}", definition.name(), id, e.getMessage());
                }
            }
        } finally {
            Intent.endDecode();
        }

        Kord.LOGGER.info("Loaded {} key semantic definitions.", loaded);
        profiler.pop();
    }
}

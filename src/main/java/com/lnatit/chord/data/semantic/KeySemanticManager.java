package com.lnatit.chord.data.semantic;

import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.semantic.SemanticalKey;
import com.lnatit.chord.eval.context.IKeyContext;
import com.lnatit.chord.eval.intent.Intent;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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

        KeyMapping.ALL.values().forEach(key -> ((SemanticalKey) key).chord$clearSemantics());
        Intent.beginDecode();
        try {
            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                ResourceLocation id = entry.getKey();
                JsonElement json = entry.getValue();
                DataResult<KeyDefinitions> result = Codecs.KEYS_CODEC.parse(JsonOps.INSTANCE, json);
                if (result.isError()) {
                    Chord.LOGGER.warn("Failed to parse key definitions in '{}': {}", id, result.error().orElseThrow());
                    continue;
                }
                KeyDefinitions definitions = result.getOrThrow();
//            KeyDefinitions definitions = GSON.fromJson(json, KeyDefinitions.class);

                Chord.LOGGER.debug("Inspecting key definitions in '{}'...", id);
                if (!definitions.checkValid()) {
                    Chord.LOGGER.info("Context definitions in '{}' is invalid and will be ignored.", id);
                    continue;
                }
                for (KeyDefinitions.KeyDefinition keyDef : definitions.keys()) {
                    KeyMapping key = SemanticalKey.lookup(keyDef.name());
                    if (key == null) {
                        Chord.LOGGER.warn("Context '{}' not found for key semantics '{}', ignored.", keyDef.name(), id);
                        continue;
                    }
                    ((SemanticalKey) key).chord$setSemantic(keyDef.toSemantic());

//                    for (KeyDefinitions.SemanticEntry sematic : keyDef.semantics()) {
//                        for (IKeyContext context : sematic.contexts()) {
//                            ((SemanticalKey) key).chord$addSemantic(context, sematic.semantic());
//                        }
//                    }
                }
                Chord.LOGGER.info("Context definitions in '{}' loaded with {} valid keys.", id, definitions.keys().size());
            }
        } finally {
            Intent.endDecode();
        }

        profiler.pop();
    }

}

package com.lnatit.chord.data.override;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.eval.override.OverrideManager;
import com.lnatit.chord.eval.override.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class DatapackOverrideReloader extends SimpleJsonResourceReloadListener
{
    public static final DatapackOverrideReloader INSTANCE = new DatapackOverrideReloader();

    private DatapackOverrideReloader() {
        super(Codecs.GSON, "builtin_overrides");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        profiler.push("chord_builtin_overrides");
        OverrideManager.clear(Type.BUILTIN);
        OverrideManager.clear(Type.CREATOR); // Clear creator overrides as well since they are both loaded from datapacks.

        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            DataResult<OverrideDefinition> result = Codecs.OVERRIDE_DEFINITION_CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.isError()) {
                Chord.LOGGER.warn("Failed to parse builtin override in '{}': {}", id, result.error().orElseThrow());
                continue;
            }

            OverrideDefinition definition = result.getOrThrow();
            if (definition.key1().isInvalid() || definition.key2().isInvalid()) {
                Chord.LOGGER.debug("Builtin override '{}' has unmet key requirement and will be ignored.", id);
                continue;
            }

            OverrideManager.put(definition.isBuiltin() ? Type.BUILTIN : Type.CREATOR, definition.getPair(), definition.result());
            loaded++;
        }

        Chord.LOGGER.info("Loaded {} builtin override entries.", loaded);
        profiler.pop();

    }
}

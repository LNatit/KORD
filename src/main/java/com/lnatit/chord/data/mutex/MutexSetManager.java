package com.lnatit.chord.data.mutex;

import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/**
 * Reload listener for {@code mutex_sets/*}.json.
 * <p>
 * One file maps to one mutex set. If {@code namespace} is absent, the file name is used.
 * Requirements gate the whole set load, not individual mutex entries.
 */
public class MutexSetManager extends SimpleJsonResourceReloadListener {
    public static final MutexSetManager INSTANCE = new MutexSetManager();

    private MutexSetManager() {
        super(Codecs.GSON, "mutex_sets");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_mutex_sets");
        MutexSet.clear();

        int loaded = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();
            DataResult<MutexDefinition> result = Codecs.MUTEX_DEFINITIONS_CODEC.parse(JsonOps.INSTANCE, json);
            if (result.isError()) {
                Chord.LOGGER.warn("Failed to parse mutex definitions in '{}': {}", id, result.error().orElseThrow());
                continue;
            }

            MutexDefinition definitions = result.getOrThrow();
            Chord.LOGGER.debug("Inspecting mutex definitions in '{}'...", id);
            if (definitions.isInvalid()) {
                Chord.LOGGER.info("Mutex definitions in '{}' is invalid and will be ignored.", id);
                continue;
            }

            String namespace = definitions.namespace().orElseGet(() -> inferNamespace(id));

            MutexSet existed = MutexSet.get(namespace);
            new MutexSet(namespace, definitions.mutexes());
            if (existed != null) {
                Chord.LOGGER.info("Mutex set '{}' overridden by '{}'.", namespace, id);
            }
            loaded++;
        }

        Chord.LOGGER.info("Loaded {} mutex sets.", loaded);
        profiler.pop();
    }


    /**
     * Fallback namespace from resource path basename when JSON omits {@code namespace}.
     */
    private static String inferNamespace(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}

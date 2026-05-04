package com.lnatit.chord.data.mutex;

import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
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
public class MutexSetManager extends SimpleJsonResourceReloadListener<MutexDefinition> {
    public static final MutexSetManager INSTANCE = new MutexSetManager();

    private MutexSetManager() {
        super(Codecs.MUTEX_DEFINITIONS_CODEC, FileToIdConverter.json("mutex_sets"));
    }

    @Override
    protected void apply(Map<Identifier, MutexDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_mutex_sets");
        MutexSet.clear();

        int loaded = 0;
        for (Map.Entry<Identifier, MutexDefinition> entry : map.entrySet()) {
            Identifier id = entry.getKey();
            MutexDefinition definitions = entry.getValue();

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
    private static String inferNamespace(Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}

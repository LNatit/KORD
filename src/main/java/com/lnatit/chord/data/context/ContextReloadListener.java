package com.lnatit.chord.data.context;

import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.semantic.KeyContext;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ContextReloadListener extends SimpleJsonResourceReloadListener<ContextDefinition> {
    public static final ContextReloadListener INSTANCE = new ContextReloadListener();

    private ContextReloadListener() {
        super(Codecs.CONTEXT_DEFINITION_CODEC, FileToIdConverter.json("contexts"));
    }

    @Override
    protected void apply(Map<Identifier, ContextDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_contexts");
        KeyContext.init();

        int loaded = 0;
        for (Map.Entry<Identifier, ContextDefinition> entry : map.entrySet()) {
            Identifier id = entry.getKey();
            ContextDefinition definition = entry.getValue();

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
}

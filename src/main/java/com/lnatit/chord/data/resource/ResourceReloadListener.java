package com.lnatit.chord.data.resource;

import com.google.gson.JsonElement;
import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Codecs;
import com.lnatit.chord.eval.resource.Resource;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ResourceReloadListener extends SimpleJsonResourceReloadListener {
    public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

    public ResourceReloadListener() {
        super(Codecs.GSON, "resources");
    }

    private static boolean isPathInvalid(String path) {
        if (path.isBlank()) {
            return true;
        }
        return path.contains("//") || path.chars().anyMatch(Character::isWhitespace);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("chord_resources");

        Resource.clear();
        int loaded = 0;
        int merged = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            DataResult<ResourceDefinition> result = Codecs.RESOURCE_DEFINITION_CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            if (result.isError()) {
                Chord.LOGGER.warn("Failed to parse resource definition in '{}': {}", id, result.error().orElseThrow());
                continue;
            }

            ResourceDefinition definition = result.getOrThrow();
            if (definition.isInvalid()) {
                Chord.LOGGER.debug("Resource definition '{}' does not met the requirement, ignored.", id);
                continue;
            }

            String path = fallbackPath(id);
            if (definition.path().isPresent()) {
                String resolved = definition.path().get();
                if (isPathInvalid(resolved)) {
                    Chord.LOGGER.warn("Resource definition '{}' has invalid path '{}', use '{}' instead.", id, resolved, path);
                } else {
                    path = resolved;
                }
            }

            if (Resource.define(path, definition.supportsConcurrentWrites())) {
                Chord.LOGGER.debug("Resource '{}' merged with '{}'.", path, id);
                merged++;
            } else {
                Chord.LOGGER.debug("Resource '{}' defined by '{}'.", path, id);
                loaded++;
            }
        }

        Chord.LOGGER.info("Loaded {} resource definitions ({} merged to stricter rules).", loaded, merged);
        profiler.pop();

    }

    // TODO check path
    private static String fallbackPath(ResourceLocation id) {
        String path = id.getPath();
        return path.startsWith("resources/") ? path.substring("resources/".length()) : path;
    }
}

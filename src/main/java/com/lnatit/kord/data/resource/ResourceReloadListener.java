package com.lnatit.kord.data.resource;

import com.lnatit.kord.Kord;
import com.lnatit.kord.data.Codecs;
import com.lnatit.kord.eval.Resource;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ResourceReloadListener extends SimpleJsonResourceReloadListener<ResourceDefinition> {
    public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

    public ResourceReloadListener() {
        super(Codecs.RESOURCE_DEFINITION_CODEC, FileToIdConverter.json("resources"));
    }

    private static boolean isPathInvalid(String path) {
        if (path.isBlank()) {
            return true;
        }
        return path.contains("//") || path.chars().anyMatch(Character::isWhitespace);
    }

    @Override
    protected void apply(Map<Identifier, ResourceDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("kord_resources");

        Resource.clear();
        int loaded = 0;
        int merged = 0;

        for (Map.Entry<Identifier, ResourceDefinition> entry : map.entrySet()) {
            Identifier id = entry.getKey();
            ResourceDefinition definition = entry.getValue();

            if (definition.isInvalid()) {
                Kord.LOGGER.debug("Resource definition '{}' does not met the requirement, ignored.", id);
                continue;
            }

            String path = fallbackPath(id);
            if (definition.path().isPresent()) {
                String resolved = definition.path().get();
                if (isPathInvalid(resolved)) {
                    Kord.LOGGER.warn("Resource definition '{}' has invalid path '{}', use '{}' instead.", id, resolved, path);
                } else {
                    path = resolved;
                }
            }

            if (Resource.define(path, definition.supportsConcurrentWrites())) {
                Kord.LOGGER.debug("Resource '{}' merged with '{}'.", path, id);
                merged++;
            } else {
                Kord.LOGGER.debug("Resource '{}' defined by '{}'.", path, id);
                loaded++;
            }
        }

        Kord.LOGGER.info("Loaded {} resource definitions ({} merged to stricter rules).", loaded, merged);
        profiler.pop();

    }

    // TODO check path
    private static String fallbackPath(Identifier id) {
        String path = id.getPath();
        return path.startsWith("resources/") ? path.substring("resources/".length()) : path;
    }
}

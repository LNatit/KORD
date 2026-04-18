package com.lnatit.chord.data.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lnatit.chord.data.Codecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class ResourceManager extends SimpleJsonResourceReloadListener
{
    public ResourceManager() {
        super(Codecs.GSON, "resources");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> map,
            net.minecraft.server.packs.resources.ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {

    }
}

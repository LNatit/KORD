package com.lnatit.kord.data.override;

import com.lnatit.kord.Kord;
import com.lnatit.kord.data.Codecs;
import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.override.OverrideManager;
import com.lnatit.kord.override.OverrideType;
import com.lnatit.kord.result.ConflictResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class DatapackOverrideReloader extends SimpleJsonResourceReloadListener<OverrideDefinition>
{
    public static final DatapackOverrideReloader INSTANCE = new DatapackOverrideReloader();

    private DatapackOverrideReloader() {
        super(Codecs.OVERRIDE_DEFINITION_CODEC, FileToIdConverter.json("overrides"));
    }

    @Override
    protected void apply(
            Map<Identifier, OverrideDefinition> map,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        profiler.push("kord_overrides");
        OverrideManager.clear(OverrideType.BUILTIN);
        OverrideManager.clear(OverrideType.CREATOR); // Clear creator overrides as well since they are both loaded from datapacks.

        int loaded = 0;
        for (Map.Entry<Identifier, OverrideDefinition> entry : map.entrySet()) {
            Identifier id = entry.getKey();
            OverrideDefinition definition = entry.getValue();

            if (definition.key1().isInvalid() || definition.key2().isInvalid()) {
                Kord.LOGGER.debug("Builtin override '{}' has unmet key requirement and will be ignored.", id);
                continue;
            }

            OverrideType type = definition.isBuiltin() ? OverrideType.BUILTIN : OverrideType.CREATOR;
            KeyPair pair = definition.getPair();
            if (pair == null) {
                Kord.LOGGER.warn(
                        "Override '{}' references unknown key(s): key1='{}', key2='{}', ignored.",
                        id,
                        definition.key1().name(),
                        definition.key2().name());
                continue;
            }

            KeyMapping left = KeyMapping.ALL.get(pair.leftId());
            KeyMapping right = KeyMapping.ALL.get(pair.rightId());
            if (left == null || right == null) {
                Kord.LOGGER.warn(
                        "Override '{}' references stale key mapping(s): pair='{}', ignored.",
                        id,
                        pair);
                continue;
            }

            ConflictResult overrideResult = new ConflictResult(left, right, definition.result().toFinalized(type.toOrigin()));
            OverrideManager.put(type, pair, overrideResult);
            loaded++;
        }

        Kord.LOGGER.info("Loaded {} builtin override entries.", loaded);
        profiler.pop();

    }
}

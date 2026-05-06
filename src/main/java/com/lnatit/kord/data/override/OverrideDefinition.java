package com.lnatit.kord.data.override;

import com.lnatit.kord.data.Requirement;
import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.override.Origin;
import com.lnatit.kord.result.risk.Finalized;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record OverrideDefinition(
        boolean isBuiltin,
        Key key1,
        Key key2,
        Result result
)
{
    @Nullable
    public KeyPair getPair() {
        KeyMapping key1Mapping = KeyMapping.ALL.get(key1.name());
        KeyMapping key2Mapping = KeyMapping.ALL.get(key2.name());
        if (key1Mapping == null || key2Mapping == null) {
            return null;
        }
        return KeyPair.of(key1Mapping, key2Mapping);
    }

    public record Key(
            Optional<Requirement> requirement,
            String name
    )
    {
        public boolean isInvalid() {
            return requirement().isPresent() && !requirement().get().isValid();
        }
    }

    public record Result(MutableComponent component, Severity severity) {
        public Finalized toFinalized(Origin origin) {
            return new Finalized.Overrid(component, severity, origin);
        }
    }
}

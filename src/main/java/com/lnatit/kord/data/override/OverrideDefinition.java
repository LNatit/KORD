package com.lnatit.kord.data.override;

import com.lnatit.kord.data.Requirement;
import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record OverrideDefinition(
        boolean isBuiltin,
        Key key1,
        Key key2,
        MutableComponent component,
        Severity severity
)
{
    @Nullable
    public KeyPair getPair() {
        return KeyPair.of(key1.name(), key2.name());
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
}

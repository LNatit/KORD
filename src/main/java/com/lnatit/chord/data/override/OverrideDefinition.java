package com.lnatit.chord.data.override;

import com.lnatit.chord.data.Requirement;
import com.lnatit.chord.eval.override.OverrideManager;
import com.lnatit.chord.result.legacy.ConflictResult;

import java.util.Optional;

public record OverrideDefinition(
        boolean isBuiltin,
        Key key1,
        Key key2,
        ConflictResult result
)
{
    public OverrideManager.Pair getPair() {
        return new OverrideManager.Pair(key1.name(), key2.name());
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

package com.lnatit.chord.data.override;

import com.lnatit.chord.eval.OverrideManager;
import com.lnatit.chord.result.ConflictResult;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Optional;

public record OverrideDefinition(
        Key key1,
        Key key2,
        ConflictResult result
)
{
    public OverrideManager.Pair getPair() {
        return new OverrideManager.Pair(key1.name(), key2.name());
    }


    public record Key(
            Optional<String> modid,
            Optional<String> mod_version_range,
            String name
    )
    {
        public boolean isInvalid(ArtifactVersion mod_version) {
            return false;
        }
    }
}

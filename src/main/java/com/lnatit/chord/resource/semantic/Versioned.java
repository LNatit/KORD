package com.lnatit.chord.resource.semantic;

import net.neoforged.neoforgespi.language.MavenVersionAdapter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

public interface Versioned
{
    boolean checkVersion(ArtifactVersion mod_version);

    static boolean containsVersion(ArtifactVersion version, String version_range) {
        VersionRange range = MavenVersionAdapter.createFromVersionSpec(version_range);
        return range.containsVersion(version);
    }
}

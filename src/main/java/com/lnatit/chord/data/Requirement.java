package com.lnatit.chord.data;

import net.neoforged.neoforgespi.language.MavenVersionAdapter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;

/**
 * Shared helpers for optional version-range requirements.
 */
public final class Requirement {
    private Requirement() {
    }

    public static boolean matches(Optional<String> versionRange, ArtifactVersion version) {
        return versionRange.isEmpty() || !versionOutOfRange(version, versionRange.get());
    }

    public static boolean versionOutOfRange(ArtifactVersion version, String versionRange) {
        VersionRange range = MavenVersionAdapter.createFromVersionSpec(versionRange);
        return !range.containsVersion(version);
    }
}


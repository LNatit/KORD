package com.lnatit.chord.data;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.MavenVersionAdapter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;

/**
 * Shared requirement descriptor for optional mod version constraints.
 */
public record Requirement(String modid, Optional<String> mod_version_range) {
    public Optional<? extends ModContainer> findContainer() {
        return ModList.get().getModContainerById(modid());
    }

    public boolean isValid() {
        Optional<? extends ModContainer> container = findContainer();
        if (container.isEmpty()) {
            return false;
        }
        ArtifactVersion modVersion = container.get().getModInfo().getVersion();
        return matches(mod_version_range(), modVersion);
    }

    public static boolean matches(Optional<String> versionRange, ArtifactVersion version) {
        return versionRange.isEmpty() || !versionOutOfRange(version, versionRange.get());
    }

    public static boolean versionOutOfRange(ArtifactVersion version, String versionRange) {
        VersionRange range = MavenVersionAdapter.createFromVersionSpec(versionRange);
        return !range.containsVersion(version);
    }
}


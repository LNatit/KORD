package com.lnatit.chord.data.semantic;

import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Requirement;
import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.context.IKeyContext;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.List;
import java.util.Optional;

/**
 * Data model for one key semantics definition file.
 * <p>
 * The file is scoped by {@code modid} and optional {@code mod_version_range}, then
 * each key and semantic entry can further narrow applicability via optional version ranges.
 */
public record KeyDefinitions(int version,
                             String modid,
                             Optional<String> mod_version_range,
                             List<KeyDefinition> keys)
{
    /**
     * Validates top-level mod constraints and removes version-incompatible nested entries.
     */
    public boolean checkValid() {
        Optional<? extends ModContainer> container = ModList.get().getModContainerById(modid());
        if (container.isEmpty()) {
            Chord.LOGGER.debug("Mod '{}' not found for key definitions, ignored.", modid());
            return false;
        }

        ArtifactVersion mod_version = container.get().getModInfo().getVersion();
        if (!Requirement.matches(mod_version_range(), mod_version)) {
            Chord.LOGGER.debug("Mod '{}' version '{}' does not satisfy the version requirement '{}' for key definitions, ignored.", modid(), mod_version, mod_version_range().get());
            return false;
        }
        keys().removeIf(k -> k.isInvalid(mod_version));
        if (keys().isEmpty()) {
            Chord.LOGGER.debug("All key definitions of mod '{}' are invalid for mod version '{}', ignored.", modid(), mod_version);
            return false;
        }
        return true;
    }

    /**
     * One key path with optional version gating and one-or-more semantic entries.
     */
    public record KeyDefinition(String name,
                                Optional<String> mod_version_range,
                                List<SemanticEntry> semantics)
    {
        public boolean isInvalid(ArtifactVersion mod_version) {
            if (!Requirement.matches(mod_version_range(), mod_version)) {
                return true;
            }
            semantics().removeIf(s -> s.isInvalid(mod_version));
            return semantics().isEmpty();
        }
    }

    /**
     * Semantic payload for a key under one-or-more contexts with optional version gating.
     */
    public record SemanticEntry(List<IKeyContext> contexts,
                                Optional<String> mod_version_range,
                                KeySemantic semantic)
    {
        public boolean isInvalid(ArtifactVersion mod_version) {
            return !Requirement.matches(mod_version_range(), mod_version);
        }
    }
}

package com.lnatit.chord.data.semantic;

import com.lnatit.chord.Chord;
import com.lnatit.chord.data.Requirement;
import com.lnatit.chord.semantic.ContextSemantic;
import com.lnatit.chord.eval.context.IKeyContext;
import com.lnatit.chord.semantic.KeySemantic;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.List;
import java.util.Optional;

/**
 * Data model for one key semantics definition file.
 * <p>
 * The file is scoped by {@code modid} and optional {@code mod_version_range}, then
 * each key and semantic entry can further narrow applicability via optional version ranges.
 */
public record KeyDefinitions(int version, Requirement requirement, List<KeyDefinition> keys)
{
    /**
     * Validates top-level mod constraints and removes version-incompatible nested entries.
     */
    public boolean checkValid() {
        if (!requirement().isValid()) {
            Chord.LOGGER.debug(
                    "Key definitions does not met the requirement: [modid = '{}', mod_version_range = '{}'], ignored.",
                    requirement().modid(),
                    requirement().mod_version_range());
            return false;
        }
        ArtifactVersion modVersion = requirement().findContainer().orElseThrow().getModInfo().getVersion();
        keys().removeIf(k -> k.isInvalid(modVersion));
        if (keys().isEmpty()) {
            Chord.LOGGER.debug("All key definitions of mod '{}' are invalid for mod version '{}', ignored.",
                               requirement().modid(),
                               modVersion);
            return false;
        }
        return true;
    }

    /**
     * One key path with optional version gating and one-or-more semantic entries.
     */
    public record KeyDefinition(String name, Optional<String> modVersionRange, List<SemanticEntry> semantics)
    {
        public boolean isInvalid(ArtifactVersion modVersion) {
            if (!Requirement.matches(modVersionRange(), modVersion)) {
                return true;
            }
            semantics().removeIf(s -> s.isInvalid(modVersion));
            return semantics().isEmpty();
        }

        public KeySemantic toSemantic() {

        }
    }

    /**
     * Semantic payload for a key under one-or-more contexts with optional version gating.
     */
    public record SemanticEntry(List<IKeyContext> contexts, Optional<String> modVersionRange, ContextSemantic semantic)
    {
        public boolean isInvalid(ArtifactVersion modVersion) {
            return !Requirement.matches(modVersionRange(), modVersion);
        }
    }
}

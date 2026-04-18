package com.lnatit.chord.data.mutex;

import com.lnatit.chord.Chord;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.List;
import java.util.Optional;

/**
 * Data model for one mutex set JSON file.
 * <p>
 * Each file defines exactly one set with optional {@code namespace}, optional
 * {@code requirements} (AND semantics), and a flat {@code mutexes} list.
 */
public record MutexDefinition(Optional<String> namespace, List<Requirement> requirements, List<String> mutexes) {
    /**
     * Valid when mutex entries are non-empty, within 32 entries, and every requirement matches.
     */
    public boolean isValid() {
        if (mutexes.isEmpty()) {
            return false;
        }
        if (mutexes.size() > 32) {
            Chord.LOGGER.warn("Mutex set has {} entries (max 32), ignored.", mutexes.size());
            return false;
        }
        return requirements.stream().allMatch(Requirement::isValid);
    }

    /**
     * A single mod requirement for loading the whole mutex set.
     */
    public record Requirement(String modid, Optional<String> mod_version_range) {
        public boolean isValid() {
            Optional<? extends ModContainer> container = ModList.get().getModContainerById(modid());
            if (container.isEmpty()) {
                return false;
            }

            if (mod_version_range().isEmpty()) {
                return true;
            }

            ArtifactVersion modVersion = container.get().getModInfo().getVersion();
            return com.lnatit.chord.data.Requirement.matches(mod_version_range(), modVersion);
        }
    }
}



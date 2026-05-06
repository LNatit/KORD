package com.lnatit.kord.data.mutex;

import com.lnatit.kord.Kord;
import com.lnatit.kord.data.Requirement;

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
    public boolean isInvalid() {
        if (mutexes.isEmpty()) {
            return true;
        }
        if (mutexes.size() > 32) {
            Kord.LOGGER.warn("Mutex set has {} entries (max 32), ignored.", mutexes.size());
            return true;
        }
        return requirements.stream().noneMatch(Requirement::isValid);
    }
}



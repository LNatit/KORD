package com.lnatit.chord.data.resource;

import com.lnatit.chord.data.Requirement;

import java.util.Optional;

/**
 * One resource node definition loaded from one datapack file.
 * <p>
 * The {@code path} is the canonical full path of the node (for example: {@code armors/helmet}).
 */
public record ResourceDefinition(Optional<Requirement> requirement, Optional<String> path,
                                 boolean supportsConcurrentWrites) {

    public boolean isInvalid() {
        return requirement().isPresent() && !requirement().get().isValid();
    }
}

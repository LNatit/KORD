package com.lnatit.suck.core.result;

import java.util.ArrayList;
import java.util.List;

public record ConflictResult(Severity severity, List<ConflictTag> tags)
{
    public static final ConflictResult SAFE = new ConflictResult(Severity.SAFE, List.of());

    public ConflictResult {
        tags = List.copyOf(tags);
    }

    // TODO give some params
    public static ConflictResult intentShared() {
        return SAFE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    {
        private Severity severity = Severity.SAFE;
        private final List<ConflictTag> tags = new ArrayList<>();

        private Builder() {}

        public Builder withTag(ConflictTag tag) {
            tags.add(tag);
            return this;
        }

        public Builder withDebugTag(String id) {
            return withTag(ConflictTag.debug(id));
        }

        public Builder withTag(ConflictTag tag, Severity tagSeverity) {
            if (tagSeverity.compareTo(this.severity) > 0) {
                this.severity = tagSeverity;
            }
            return withTag(tag);
        }

        public Builder withPair(ConflictTag.Pair pair) {
            return withTag(pair.tag(), pair.severity());
        }

        public ConflictResult build() {
            if (severity == Severity.SAFE && tags.isEmpty()) {
                return SAFE;
            }
            return new ConflictResult(severity, tags);
        }
    }
}

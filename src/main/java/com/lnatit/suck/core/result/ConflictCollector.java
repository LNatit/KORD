package com.lnatit.suck.core.result;

import java.util.ArrayList;
import java.util.List;

public class ConflictCollector {
    private boolean blockPipeline = false;
    private Severity severity = Severity.SAFE;
    private final List<ConflictTag> tags = new ArrayList<>();

    public ConflictCollector withTag(ConflictTag tag) {
        tags.add(tag);
        return this;
    }

    public ConflictCollector withDebugTag(String shortCode) {
        return withTag(ConflictTag.debug(shortCode));
    }

    public ConflictCollector withTag(ConflictTag tag, Severity tagSeverity) {
        if (tagSeverity.compareTo(this.severity) > 0) {
            this.severity = tagSeverity;
        }
        return withTag(tag);
    }

    public ConflictCollector withPair(ConflictTag.Pair pair) {
        return withTag(pair.tag(), pair.severity());
    }

    public void blockPipeline() {
        this.blockPipeline = true;
    }

    public boolean isBlocked() {
        return this.blockPipeline;
    }

    public ConflictResult toResult() {
        if (severity == Severity.SAFE && tags.isEmpty()) {
            return ConflictResult.SAFE;
        }
        return new ConflictResult(severity, tags);
    }
}

package com.lnatit.suck.core.result;

import com.lnatit.suck.Suck;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConflictCollector {
    private boolean finished = false;
    private Severity severity = Severity.SAFE;
    private final List<ConflictTag> tags = new ArrayList<>();
    private final List<ConflictRisk> risks = new ArrayList<>(2);

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

    public ConflictCollector withRisk(ConflictRisk risk) {
        this.risks.add(risk);
        return this;
    }

    @Nullable
    public <T extends ConflictRisk> T getRisk(Class<T> type) {
        for (ConflictRisk risk : risks) {
            if (type.isInstance(risk)) {
                return type.cast(risk);
            }
        }
        return null;
    }

    public void blockPipeline() {
        this.finished = true;
    }

    public boolean finished() {
        return this.finished;
    }

    public ConflictResult toResult() {
        if (!risks.isEmpty()) {
            Suck.LOGGER.warn("Risks are not all handled! Risks: {}", risks);
        }

        if (severity == Severity.SAFE && tags.isEmpty()) {
            return ConflictResult.SAFE;
        }
        return new ConflictResult(severity, tags);
    }
}

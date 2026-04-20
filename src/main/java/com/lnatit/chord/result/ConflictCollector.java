package com.lnatit.chord.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ConflictCollector
{
    private final List<ConflictRisk> risks = new ArrayList<>();

    public final void withRisk(ConflictRisk risk) {
        this.risks.add(risk);
    }

    public final void withRisk(ConflictTag tag, Severity severity) {
        this.withRisk(ConflictRisk.create(tag, severity));
    }

    public final void withDebug(ConflictTag debugTag) {
        this.withRisk(ConflictRisk.create(debugTag, Severity.SAFE));
    }

    protected final void mergeFrom(ConflictCollector collector) {
        this.risks.addAll(collector.risks);
    }

    protected final List<ConflictRisk> snapshotRisks() {
        return List.copyOf(this.risks);
    }

    protected final <R extends DynamicRisk> Optional<R> getRiskByType(Class<R> type) {
        for (ConflictRisk risk : this.risks) {
            if (type.isInstance(risk)) {
                return Optional.of(type.cast(risk));
            }
        }
        return Optional.empty();
    }

    protected final Severity resolveSeverity() {
        return resolveSeverity(this.risks);
    }

    protected static Severity resolveSeverity(List<ConflictRisk> risks) {
        Severity severity = Severity.SAFE;
        for (ConflictRisk risk : risks) {
            if (risk.severity().ordinal() > severity.ordinal()) {
                severity = risk.severity();
            }
        }
        return severity;
    }
}

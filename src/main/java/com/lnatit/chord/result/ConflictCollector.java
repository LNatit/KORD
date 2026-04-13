package com.lnatit.chord.result;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConflictCollector
{
    private boolean finished = false;
    private final List<ConflictRisk> risks = new ArrayList<>();

    public void withRisk(ConflictRisk risk) {
        this.risks.add(risk);
    }

    public void withRisk(ConflictTag tag, Severity severity) {
        this.withRisk(ConflictRisk.create(tag, severity));
    }

    public void withDebug(ConflictTag debugTag) {
        this.withRisk(ConflictRisk.create(debugTag, Severity.SAFE));
    }

    public void merge(ConflictCollector collector) {
        this.risks.addAll(collector.risks);
    }

    // TODO use Optional<R> later
    @Nullable
    public <R extends DynamicRisk> R getRisk(Class<R> type) {
        for (ConflictRisk risk : risks) {
            if (type.isInstance(risk)) {
                return type.cast(risk);
            }
        }
        return null;
    }

    public void setFinished() {
        this.finished = true;
    }

    public boolean finished() {
        return this.finished;
    }

    public ConflictResult toResult() {
        Severity severity = Severity.SAFE;
        for (ConflictRisk risk : risks) {
            if (risk.severity().ordinal() > severity.ordinal()) {
                severity = risk.severity();
            }
        }
        return new ConflictResult(severity, this.risks);
    }
}

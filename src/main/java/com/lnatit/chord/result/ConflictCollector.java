package com.lnatit.chord.result;

import java.util.*;

public abstract class ConflictCollector
{
    protected final ArrayList<ConflictRisk> risks = new ArrayList<>();

    public final void withRisk(ConflictRisk risk) {
        this.risks.add(risk);
    }

    public final void withRisk(ConflictTag tag, Severity severity) {
        this.withRisk(ConflictRisk.of(tag, severity));
    }

    public final void withDebug(ConflictTag debugTag) {
        this.withRisk(ConflictRisk.of(debugTag, Severity.SAFE));
    }

    public final <R extends DynamicRisk> Optional<R> getRiskOf(Class<R> type) {
        for (ConflictRisk risk : this.risks) {
            if (type.isInstance(risk)) {
                return Optional.of(type.cast(risk));
            }
        }
        return Optional.empty();
    }

    public static Meta meta() {
        return new Meta();
    }

    public static Context context() {
        return new Context();
    }

    public static class Meta extends ConflictCollector
    {
        private final Map<ContextPair, List<ConflictRisk>> contextRisks = new LinkedHashMap<>();

        private Meta() {}

        public void merge(ContextPair pair, Context context) {
            contextRisks.put(pair, context.risks);
        }

        public ConflictResult toResult() {
            List<ConflictResult.ContextRisk> contextRisks = new ArrayList<>();
            for (Map.Entry<ContextPair, List<ConflictRisk>> entry : this.contextRisks.entrySet()) {
                contextRisks.add(new ConflictResult.ContextRisk(entry.getKey(), entry.getValue()));
            }
            return new ConflictResult(this.risks, contextRisks);
        }
    }

    public static class Context extends ConflictCollector
    {
        private boolean finished = false;

        private Context() {}

        public void setFinished() {
            this.finished = true;
        }

        public boolean finished() {
            return this.finished;
        }
    }
}


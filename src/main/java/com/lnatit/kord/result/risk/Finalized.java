package com.lnatit.kord.result.risk;

import com.lnatit.kord.semantic.KeyContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

// Display Interface
public interface Finalized extends ConflictRisk
{
    Finalized CONTEXT_MUTEX = Finalized.of(RiskTag.CONTEXT_MUTEX);

    @Override
    default boolean isHidden() {
        return false;
    }

    static Finalized of(RiskTag tag) {
        return new Custom(RiskEntry.diagnostic(tag));
    }

    static Finalized ofPairs(List<KeyContext.Pair> pairs) {
        return new Custom(new RiskEntry.ContextPairs(pairs));
    }

    record Custom(RiskEntry<?> holder) implements Finalized
    {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public Custom {}

        @Override
        public Severity severity() {
            return this.holder.severity();
        }
    }

    record Pipeline(List<Mapped<KeyContext, Packed>> byContextRisks, Severity severity) implements Finalized
    {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public Pipeline {}

        public Pipeline(Map<KeyContext, Packed> byContextRisks) {
            this(Mapped.of(byContextRisks), ConflictRisk.resolveSeverity(byContextRisks.values()));
        }
    }
}

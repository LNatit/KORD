package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

// Display Interface
public interface Finalized extends ConflictRisk
{
    @Override
    default boolean isHidden() {
        return false;
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

    record Custom(List<Mapped<KeyContext.Pair, RiskEntry<?>>> byPairRisks, Severity severity) implements Finalized
    {
        public static final Custom EMPTY = new Custom(List.of(), Severity.SAFE);

        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public Custom {}

        public Custom(Map<KeyContext.Pair, RiskEntry<?>> byPairRisks) {
            this(Mapped.of(byPairRisks), ConflictRisk.resolveSeverity(byPairRisks.values()));
        }
    }

}

package com.lnatit.chord.result;

import com.lnatit.chord.eval.override.Origin;
import com.lnatit.chord.semantic.KeyContext;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

// Display Interface
public interface Finalized extends ConflictRisk
{
    Finalized HARDWARE_INPUT = Finalized.of(RiskTag.HARDWARE_INPUT);
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

    record Overrid(MutableComponent component, Severity severity, Origin origin) implements Finalized
    {}

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

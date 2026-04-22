package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;
import net.minecraft.client.KeyMapping;

import java.util.List;
import java.util.Map;

// Display Interface
public interface ConflictResult extends ConflictRisk
{
    @Override
    default boolean isDiagnostic() {
        return false;
    }

    record Pipeline(List<Mapped<KeyContext, Packed>> byContextRisks, Severity severity) implements ConflictResult {
        public Pipeline(Map<KeyContext, Packed> byContextRisks) {
            this(Mapped.of(byContextRisks), ConflictRisk.resolveSeverity(byContextRisks.values()));
        }
    }

    record Custom(List<Mapped<KeyContext.CustomPair, ConflictRisk>> byPairRisks, Severity severity) implements ConflictResult {
        public static final Custom EMPTY = new Custom(List.of(), Severity.SAFE);

        public Custom(Map<KeyContext.CustomPair, ConflictRisk> byPairRisks) {
            this(Mapped.of(byPairRisks), ConflictRisk.resolveSeverity(byPairRisks.values()));
        }
    }

    // TODO use a wrapped KeyPair for Map key
    record Impl(KeyMapping left, KeyMapping right, ConflictResult result) implements ConflictResult
    {
        public Impl {
            if (left.compareTo(right) > 0) {
                // swap to ensure left <= right
                KeyMapping temp = left;
                left = right;
                right = temp;
            }
        }

        @Override
        public Severity severity() {
            return this.result.severity();
        }
    }
}

package com.lnatit.chord.result;

import com.lnatit.chord.util.Provider;
import com.lnatit.chord.util.Supplier;

@FunctionalInterface
public interface ConflictInfo
{
    void attachTo(ConflictCollector collector);

    private static Provider<ConflictInfo> of(Provider<ConflictRisk> riskProvider, boolean meltdown) {
        return isReal -> collector -> {
            collector.withRisk(riskProvider.get(isReal));
            if (meltdown) {collector.setFinished();}
        };
    }

    static Provider<ConflictInfo> simple(Provider<ConflictRisk> riskProvider) {
        return of(riskProvider, false);
    }

    static Provider<ConflictInfo> simple(Supplier<ConflictRisk> riskSupplier) {
        return simple((Provider<ConflictRisk>) riskSupplier);
    }

    static Provider<ConflictInfo> meltdown(Provider<ConflictRisk> riskProvider) {
        return of(riskProvider, true);
    }

    static Provider<ConflictInfo> meltdown(Supplier<ConflictRisk> riskSupplier) {
        return meltdown((Provider<ConflictRisk>) riskSupplier);
    }
}

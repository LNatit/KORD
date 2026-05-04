package com.lnatit.chord.eval;

import com.lnatit.chord.result.risk.RiskEntry;
import com.lnatit.chord.result.risk.Severity;
import com.lnatit.chord.result.risk.context.ContextCollector;
import com.lnatit.chord.result.risk.context.RedirectTag;
import com.lnatit.chord.util.AsymmetricEnumMatrix;
import com.lnatit.chord.util.Provider;
import com.lnatit.chord.util.Supplier;

public enum RedirectMode
{
    NONE, KEY, MOUSE, ALL;

    // context_clash
    public static final AsymmetricEnumMatrix<RedirectMode, Info> MATRIX = new AsymmetricEnumMatrix<>(
            RedirectMode.class,
            meltdown(RiskEntry.create(RedirectTag.CONTEXT_CLASH, Severity.SEVERE)));

    static {
        // no_redirect
        MATRIX.put(NONE, NONE, simple(RiskEntry.create(RedirectTag.NO_REDIRECT, Severity.SAFE)));

        MATRIX.put(KEY, NONE, simple(RedirectTag.ContextLeak::new));
        MATRIX.put(KEY, KEY, simple(RedirectTag.DeferredRisk::new));

        MATRIX.put(MOUSE, NONE, simple(RedirectTag.LoseFocus::new));
        MATRIX.put(KEY, MOUSE, simple(RedirectTag.InputBlock::new));
        // focus_collision
        MATRIX.put(MOUSE, MOUSE, meltdown(RiskEntry.create(RedirectTag.FOCUS_COLLISION, Severity.SEVERE)));
    }

    @FunctionalInterface
    public interface Info
    {
        boolean attachTo(ContextCollector collector);
    }

    private static Provider<Info> simple(Provider<RiskEntry<RedirectTag>> riskProvider) {
        return isReal -> collector -> {
            collector.setRedirect(riskProvider.get(isReal));
            return false;
        };
    }

    private static Provider<Info> simple(Supplier<RiskEntry<RedirectTag>> riskSupplier) {
        return isReal -> collector -> {
            collector.setRedirect(riskSupplier.get());
            return false;
        };
    }

    private static Info simple(RiskEntry<RedirectTag> risk) {
        return collector -> {
            collector.setRedirect(risk);
            return false;
        };
    }

    private static Info meltdown(RiskEntry<RedirectTag> risk) {
        return collector -> {
            collector.setRedirect(risk);
            return true;
        };
    }
}

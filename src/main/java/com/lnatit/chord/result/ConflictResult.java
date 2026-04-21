package com.lnatit.chord.result;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

// TODO split meta and context, calculate severity on each Context
public record ConflictResult(Severity severity, List<ConflictRisk> metaRisks, List<ContextRisk> contextRisks)
{
    public static final ConflictResult SAFE = new ConflictResult(Severity.SAFE, List.of(), List.of());

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("all")
    public ConflictResult {
        metaRisks = List.copyOf(metaRisks);
        contextRisks = List.copyOf(contextRisks);
    }

    public ConflictResult(List<ConflictRisk> metaRisks, List<ContextRisk> contextRisks) {
        this(resolveSeverity(metaRisks, contextRisks), metaRisks, contextRisks);
    }

    public record ContextRisk(ContextPair pair,
                              Severity severity,
                              List<ConflictRisk> risks) implements Severity.Supplier
    {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public ContextRisk {
            risks = List.copyOf(risks);
        }

        public ContextRisk(ContextPair pair, List<ConflictRisk> risks) {
            this(pair, resolveSeverity(risks), risks);
        }
    }

    @SafeVarargs
    private static Severity resolveSeverity(List<? extends Severity.Supplier>... lists) {
        Severity severity = Severity.SAFE;
        for (List<? extends Severity.Supplier> list : lists) {
            for (Severity.Supplier supplier : list) {
                Severity s = supplier.severity();
                if (s == Severity.SEVERE) {
                    return s;
                }
                else if (s.ordinal() > severity.ordinal()) {
                    severity = s;
                }
            }
        }
        return severity;
    }
}

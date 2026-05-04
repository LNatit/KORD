package com.lnatit.chord.result.risk;

public interface RiskTag
{
    RiskTag HARDWARE_INPUT = of("hardware_input");
    RiskTag CONTEXT_MUTEX = of("context_mutex");
    RiskTag CONTEXT_CONFLICT = of("context_conflict");

    static RiskTag of(String id) {
        return new Simple(id);
    }

    record Simple(String id) implements RiskTag {
    }
}

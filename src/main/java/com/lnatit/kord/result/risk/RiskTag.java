package com.lnatit.kord.result.risk;

public interface RiskTag
{
    RiskTag HARDWARE_INPUT = of("hardware_input");
    RiskTag CONTEXT_MUTEX = of("context_mutex");
    RiskTag CONTEXT_CONFLICT = of("context_conflict");

    String id();

    default String translationKey() {
        return "kord.risk." + id();
    }

    static RiskTag of(String id) {
        return new Simple(id);
    }

    record Simple(String id) implements RiskTag {
    }
}

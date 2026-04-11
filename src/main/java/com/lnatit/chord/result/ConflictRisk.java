package com.lnatit.chord.result;

public non-sealed interface ConflictRisk extends ConflictInfo
{
    ConflictTag tag();

    Severity severity();

    default void attachTo(ConflictCollector collector) {
        collector.withRisk(this);
    }

    static ConflictRisk of(ConflictTag tag, Severity severity) {
        return new Static(tag, severity);
    }

    record Static(ConflictTag tag, Severity severity) implements ConflictRisk
    {}
}

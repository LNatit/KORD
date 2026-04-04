package com.lnatit.suck.core.result;

public sealed interface ConflictInfo permits ConflictRisk, ConflictTag.Pair {
    void attachTo(ConflictCollector collector);
}

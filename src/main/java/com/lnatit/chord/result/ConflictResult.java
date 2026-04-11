package com.lnatit.chord.result;

import java.util.List;

public record ConflictResult(Severity severity, List<ConflictRisk> risks)
{
    public static final ConflictResult SAFE = new ConflictResult(Severity.SAFE, List.of());

    public ConflictResult {
        risks = List.copyOf(risks);
    }
}

package com.lnatit.suck.core.result;

import java.util.List;

public record ConflictResult(Severity severity, List<ConflictTag> tags)
{
    public static final ConflictResult SAFE = new ConflictResult(Severity.SAFE, List.of());

    public ConflictResult {
        tags = List.copyOf(tags);
    }
}

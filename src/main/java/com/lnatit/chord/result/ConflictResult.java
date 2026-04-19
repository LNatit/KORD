package com.lnatit.chord.result;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record ConflictResult(Severity severity, List<ConflictRisk> metaRisks, Map<ContextPair, List<ConflictRisk>> pairRisks)
{
    public static final ConflictResult SAFE = new ConflictResult(Severity.SAFE, List.of(), Map.of());

    public ConflictResult {
        metaRisks = List.copyOf(metaRisks);
        pairRisks = pairRisks.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())
                ));
    }

    public ConflictResult(Severity severity, List<ConflictRisk> risks) {
        this(severity, risks, Map.of());
    }

    // Backward-compatible flattened view.
    public List<ConflictRisk> risks() {
        return Stream.concat(
                        this.metaRisks.stream(),
                        this.pairRisks.values().stream().flatMap(List::stream)
                )
                .toList();
    }
}

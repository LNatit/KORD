package com.lnatit.chord.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetaConflictCollector extends ConflictCollector
{
    private final Map<ContextPair, List<ConflictRisk>> pairRisks = new HashMap<>();

    public void mergePair(ContextPair pair, PairConflictCollector collector) {
        List<ConflictRisk> risks = collector.snapshotRisks();
        if (risks.isEmpty()) {
            return;
        }
        this.pairRisks.computeIfAbsent(pair, p -> new ArrayList<>()).addAll(risks);
    }

    public ConflictResult toResult() {
        Severity severity = this.resolveSeverity();
        Map<ContextPair, List<ConflictRisk>> immutablePairRisks = new HashMap<>();
        for (Map.Entry<ContextPair, List<ConflictRisk>> entry : this.pairRisks.entrySet()) {
            List<ConflictRisk> pairRiskList = List.copyOf(entry.getValue());
            immutablePairRisks.put(entry.getKey(), pairRiskList);
            Severity pairSeverity = ConflictCollector.resolveSeverity(pairRiskList);
            if (pairSeverity.ordinal() > severity.ordinal()) {
                severity = pairSeverity;
            }
        }
        return new ConflictResult(severity, this.snapshotRisks(), immutablePairRisks);
    }
}


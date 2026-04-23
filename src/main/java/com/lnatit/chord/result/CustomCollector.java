package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomCollector {
    private final Map<KeyContext.Pair, RiskEntry> byPairRisks = new LinkedHashMap<>();

    public void add(KeyContext.Pair pair, RiskEntry risk) {
        this.byPairRisks.put(pair, risk);
    }

    public ConflictResult.Custom collect() {
        return byPairRisks.isEmpty() ? ConflictResult.Custom.EMPTY : new ConflictResult.Custom(this.byPairRisks);
    }
}

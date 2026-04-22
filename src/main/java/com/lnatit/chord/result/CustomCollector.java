package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomCollector
{
    private final Map<KeyContext.CustomPair, ConflictRisk> byPairRisks = new LinkedHashMap<>();

    public void add(KeyContext.CustomPair pair, ConflictRisk risk)
    {
        this.byPairRisks.put(pair, risk);
    }

    public ConflictResult.Custom collect() {
        if (byPairRisks.isEmpty())
        {
            return ConflictResult.Custom.EMPTY;
        }
        // is copy really needed since no further modification?
        return new ConflictResult.Custom(this.byPairRisks);
    }
}

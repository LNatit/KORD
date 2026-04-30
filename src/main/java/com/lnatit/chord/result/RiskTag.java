package com.lnatit.chord.result;

public interface RiskTag
{

    static RiskTag of(String id) {
        return new Literal(id);
    }

    record Literal(String id) implements RiskTag {

    }
}

package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum InterceptTag implements RiskTag
{
    RACE_CONDITION("i_race_condition"),
    INTERCEPT_INPUT("i_intercept_input"),
    PARTIAL_OVERRIDE("i_partial_override"),
    CONCURRENT_INPUT("i_cuncurrent_input");

    private final String id;

    InterceptTag(String id) {this.id = id;}

    @Override
    public String id() {
        return this.id;
    }
}

package com.lnatit.chord.result.context;

import com.lnatit.chord.result.RiskTag;

public enum InterceptTag implements RiskTag
{
    RACE_CONDITION,
    INTERCEPT_INPUT,
    PARTIAL_OVERRIDE,
    CONCURRENT_INPUT
}

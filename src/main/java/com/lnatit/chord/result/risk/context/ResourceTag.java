package com.lnatit.chord.result.risk.context;

import com.lnatit.chord.result.risk.RiskTag;

public enum ResourceTag implements RiskTag
{
    RESOURCE_MUTEX,
    CONCURRENT_ACCESS,
    CONCURRENT_WRITE,
    READ_WRITE
}

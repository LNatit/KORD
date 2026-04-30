package com.lnatit.chord.result.context;

import com.lnatit.chord.result.RiskTag;

public enum ResourceTag implements RiskTag
{
    RESOURCE_MUTEX,
    CONCURRENT_ACCESS,
    CONCURRENT_WRITE,
    READ_WRITE
}

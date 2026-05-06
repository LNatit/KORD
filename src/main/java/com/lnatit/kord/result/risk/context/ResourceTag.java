package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum ResourceTag implements RiskTag
{
    RESOURCE_MUTEX,
    CONCURRENT_ACCESS,
    CONCURRENT_WRITE,
    READ_WRITE
}

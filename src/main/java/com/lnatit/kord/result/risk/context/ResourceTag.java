package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum ResourceTag implements RiskTag
{
    RESOURCE_MUTEX("r_resource_mutex"),
    CONCURRENT_ACCESS("r_concurrent_access"),
    CONCURRENT_WRITE("r_concurrent_write"),
    READ_WRITE("r_read_write");

    private final String id;

    ResourceTag(String id) {this.id = id;}

    @Override
    public String id() {
        return this.id;
    }
}

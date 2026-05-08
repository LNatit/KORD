package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum IntentTag implements RiskTag
{
    INTENT_SHARE("t_intent_share"),
    INTENT_IRRELEVANT("t_intent_irrelevant");

    private final String id;

    IntentTag(String id) {this.id = id;}

    @Override
    public String id() {
        return this.id;
    }
}

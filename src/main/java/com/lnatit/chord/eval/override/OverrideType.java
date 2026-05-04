package com.lnatit.chord.eval.override;

import com.lnatit.chord.result.risk.RiskTag;

public enum OverrideType
{
    USER(RiskTag.of("user_override")),
    BUILTIN(RiskTag.of("builtin_override")),
    CREATOR(RiskTag.of("creator_override")),
    PLAYER(RiskTag.of("player_override"));

    private final RiskTag tag;

    OverrideType(RiskTag tag) {
        this.tag = tag;
    }

    public RiskTag tag() {
        return tag;
    }

    public Origin toOrigin() {
        return switch (this) {
            case BUILTIN -> Origin.BUILTIN;
            case CREATOR -> Origin.CREATOR;
            case USER -> Origin.USER;
            case PLAYER -> Origin.PLAYER;
        };
    }
}

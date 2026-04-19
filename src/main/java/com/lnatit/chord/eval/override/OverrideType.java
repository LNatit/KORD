package com.lnatit.chord.eval.override;

import com.lnatit.chord.result.ConflictTag;

public enum OverrideType
{
    USER(ConflictTag.USER_OVERRIDE),
    BUILTIN(ConflictTag.BUILTIN_OVERRIDE),
    CREATOR(ConflictTag.CREATOR_OVERRIDE),
    PLAYER(ConflictTag.PLAYER_OVERRIDE);

    private final ConflictTag tag;

    OverrideType(ConflictTag tag) {
        this.tag = tag;
    }

    public ConflictTag tag() {
        return tag;
    }
}

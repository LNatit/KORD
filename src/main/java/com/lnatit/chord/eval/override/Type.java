package com.lnatit.chord.eval.override;

import com.lnatit.chord.result.ConflictTag;

public enum Type
{
    USER(ConflictTag.USER_OVERRIDE),
    BUILTIN(ConflictTag.BUILTIN_OVERRIDE),
    CREATOR(ConflictTag.CREATOR_OVERRIDE),
    PLAYER(ConflictTag.PLAYER_OVERRIDE);

    private final ConflictTag tag;

    Type(ConflictTag tag) {
        this.tag = tag;
    }

    public ConflictTag tag() {
        return tag;
    }
}

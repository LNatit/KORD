package com.lnatit.chord.eval;

import com.lnatit.chord.result.*;
import com.lnatit.chord.result.legacy.ConflictInfo;
import com.lnatit.chord.result.legacy.ConflictRisk;
import com.lnatit.chord.result.ConflictTag;
import com.lnatit.chord.result.legacy.DynamicRisk;
import com.lnatit.chord.util.AsymmetricEnumMatrix;

public enum RedirectMode
{
    NONE, KEY, MOUSE, ALL;

    // context_clash
    public static final AsymmetricEnumMatrix<RedirectMode, ConflictInfo> MATRIX = new AsymmetricEnumMatrix<>(
            RedirectMode.class,
            ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.CONTEXT_CLASH, Severity.SEVERE)).get(true));

    static {
        // no_redirect
        MATRIX.put(NONE, NONE, ConflictInfo.simple(ConflictRisk.of(ConflictTag.NO_REDIRECT, Severity.SAFE)));

        MATRIX.put(KEY, NONE, ConflictInfo.simple(DynamicRisk.ContextLeak::new));
        MATRIX.put(KEY, KEY, ConflictInfo.simple(DynamicRisk.DeferredRisk::new));

        MATRIX.put(MOUSE, NONE, ConflictInfo.simple(DynamicRisk.LoseFocus::new));
        MATRIX.put(KEY, MOUSE, ConflictInfo.simple(DynamicRisk.InputBlock::new));
        // focus_collision
        MATRIX.put(MOUSE, MOUSE, ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.FOCUS_COLLISION, Severity.SEVERE)));
    }
}

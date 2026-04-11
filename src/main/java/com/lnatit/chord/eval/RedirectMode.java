package com.lnatit.chord.eval;

import com.lnatit.chord.result.*;
import com.lnatit.chord.util.SymmetricEnumMatrix;

public enum RedirectMode
{
    NONE, KEY, MOUSE, ALL;

    // context_clash
    public static final SymmetricEnumMatrix<RedirectMode, ConflictInfo> MATRIX =
            new SymmetricEnumMatrix<>(RedirectMode.class,
                                      ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.CONTEXT_CLASH,
                                                                            Severity.SEVERE)));

    static {
        // no_redirect
        MATRIX.put(NONE, NONE, ConflictRisk.of(ConflictTag.NO_REDIRECT, Severity.SAFE));

        MATRIX.put(KEY, NONE, new DynamicRisk.DeferredRisk(false));
        MATRIX.put(KEY, KEY, new DynamicRisk.DeferredRisk(true));

        MATRIX.put(NONE, MOUSE, new DynamicRisk.LoseFocus());
        MATRIX.put(KEY, MOUSE, new DynamicRisk.InputBlock());
        // focus_collision
        MATRIX.put(MOUSE, MOUSE, ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.FOCUS_COLLISION, Severity.SEVERE)));
    }
}

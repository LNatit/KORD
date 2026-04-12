package com.lnatit.chord.eval;

import com.lnatit.chord.result.*;
import com.lnatit.chord.util.SymmetricEnumMatrix;

public enum RedirectMode
{
    NONE, KEY, MOUSE, ALL;

    // TODO dynamic risk need factory instead of value ref
    // context_clash
    public static final SymmetricEnumMatrix<RedirectMode, ConflictInfo> MATRIX =
            new SymmetricEnumMatrix<>(RedirectMode.class,
                                      ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.CONTEXT_CLASH,
                                                                            Severity.SEVERE)));

    static {
        // no_redirect
        MATRIX.put(NONE, NONE, ConflictRisk.of(ConflictTag.NO_REDIRECT, Severity.SAFE));

        // TODO
        MATRIX.put(KEY, NONE, new DynamicRisk.DeferredRisk());
        MATRIX.put(KEY, KEY, new DynamicRisk.DeferredRisk());

        MATRIX.put(NONE, MOUSE, new DynamicRisk.LoseFocus(false));
        MATRIX.put(KEY, MOUSE, new DynamicRisk.InputBlock());
        // focus_collision
        MATRIX.put(MOUSE, MOUSE, ConflictInfo.meltdown(ConflictRisk.of(ConflictTag.FOCUS_COLLISION, Severity.SEVERE)));
    }
}

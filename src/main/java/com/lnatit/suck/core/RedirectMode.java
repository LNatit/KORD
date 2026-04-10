package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictInfo;
import com.lnatit.suck.core.result.ConflictRisk;
import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.result.Severity;
import com.lnatit.suck.core.util.SymmetricEnumMatrix;

public enum RedirectMode {
    NONE,
    KEY,
    MOUSE,
    ALL;

    // context_clash
    public static final SymmetricEnumMatrix<RedirectMode, ConflictInfo> MATRIX =
            new SymmetricEnumMatrix<>(RedirectMode.class, new ConflictTag.Pair(ConflictTag.simple("r_cc"), Severity.SEVERE, true));

    static {
        // no_redirect
        MATRIX.put(NONE, NONE, new ConflictTag.Pair(ConflictTag.simple("r_nr"), Severity.SAFE));

        MATRIX.put(KEY, NONE, new ConflictRisk.DeferredRisk(false));
        MATRIX.put(KEY, KEY, new ConflictRisk.DeferredRisk(true));

        MATRIX.put(NONE, MOUSE, new ConflictRisk.LoseFocus());
        MATRIX.put(KEY, MOUSE, new ConflictRisk.InputBlock());
        // focus_collision
        MATRIX.put(MOUSE, MOUSE, new ConflictTag.Pair(ConflictTag.simple("r_fc"), Severity.SEVERE, true));
    }
}

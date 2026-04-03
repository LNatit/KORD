package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.result.Severity;
import com.lnatit.suck.core.util.SymmetricEnumMatrix;

public enum Modality
{
    PRESS, HOLD, TOGGLE, CYCLE;

    public static final ConflictTag MATCH = ConflictTag.debug("modality_match");
    public static final ConflictTag MISMATCH = ConflictTag.simple("timing_mismatch");
    public static final ConflictTag DISORDER = ConflictTag.simple("sequence_disorder");
    public static final ConflictTag DESYNC = ConflictTag.simple("state_desync");


    public static final ConflictTag.Pair DESYNC;

    public static final SymmetricEnumMatrix<Modality, ConflictTag.Pair> MATRIX =
            new SymmetricEnumMatrix<>(Modality.class, MISMATCH.withSeverity(Severity.INFO));

    static {
        ConflictTag.Pair pair = MATCH.withSeverity(Severity.SAFE);
        MATRIX.put(PRESS, PRESS, pair);
        MATRIX.put(HOLD, HOLD, pair);

        MATRIX.put(TOGGLE, TOGGLE, DISORDER.withSeverity(Severity.WARNING));
        MATRIX.putAll(CYCLE, DISORDER.withSeverity(Severity.SEVERE), TOGGLE, CYCLE);

        MATRIX.putAll();
        MATRIX.putAll(HOLD, DESYNC.withSeverity(Severity.SEVERE), TOGGLE, CYCLE);
    }
}

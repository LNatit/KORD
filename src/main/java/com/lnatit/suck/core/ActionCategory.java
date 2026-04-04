package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.util.SymmetricEnumMatrix;

public interface ActionCategory
{
    enum InGame implements ActionCategory
    {
        MOVEMENT,
        CORE_ACT,
        ABILITY,
        OVERLAY,
        INFO,
        SYSTEM;

        public static final SymmetricEnumMatrix<InGame, ConflictTag.Pair> MATRIX =
                ;


    }

    enum InGui implements ActionCategory
    {

        A;

        public static final SymmetricEnumMatrix<InGui, ConflictTag.Pair> MATRIX =
                ;
    }
}

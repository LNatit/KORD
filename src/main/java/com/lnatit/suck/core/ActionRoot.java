package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.util.SymmetricEnumMatrix;

public interface ActionRoot
{
    enum InGame implements ActionRoot
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

    enum InGui implements ActionRoot
    {

        A;

        public static final SymmetricEnumMatrix<InGui, ConflictTag.Pair> MATRIX =
                ;
    }
}

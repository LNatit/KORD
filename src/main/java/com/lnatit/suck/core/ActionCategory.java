package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.result.Severity;
import com.lnatit.suck.core.util.SymmetricEnumMatrix;

public interface ActionCategory {
    enum InGame implements ActionCategory {
        MOVEMENT, CORE_ACT, ABILITY, DISPLAY, SYSTEM;

        public static final SymmetricEnumMatrix<InGame, ConflictTag.Pair> MATRIX = new SymmetricEnumMatrix<>(InGame.class, new ConflictTag.Pair(ConflictTag.debug("a_co"), Severity.SAFE));

        static {
            ConflictTag tag = ConflictTag.simple("a_cm");
            // companion_movement
            MATRIX.put(MOVEMENT, MOVEMENT, new ConflictTag.Pair(tag, Severity.INFO));
            MATRIX.put(MOVEMENT, CORE_ACT, new ConflictTag.Pair(tag, Severity.WARNING));

            // frequent_companion
            ConflictTag.Pair pair = new ConflictTag.Pair(ConflictTag.simple("a_fc"), Severity.INFO);
            MATRIX.putAll(MOVEMENT, pair, ABILITY, DISPLAY, SYSTEM);
            MATRIX.putAll(CORE_ACT, pair, DISPLAY, SYSTEM);

            // action_clash
            MATRIX.put(CORE_ACT, CORE_ACT, new ConflictTag.Pair(ConflictTag.simple("a_ac"), Severity.SEVERE));

            // companion_ability
            MATRIX.putAll(ABILITY, new ConflictTag.Pair(ConflictTag.simple("a_ca"), Severity.WARNING), CORE_ACT, ABILITY);
        }

    }

    enum InGui implements ActionCategory {
        MOUSE_OP, LOCAL_OP, GLOBAL_OP, SYSTEM;

        public static final SymmetricEnumMatrix<InGui, ConflictTag.Pair> MATRIX = ;
    }
}

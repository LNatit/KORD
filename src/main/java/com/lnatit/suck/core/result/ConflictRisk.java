package com.lnatit.suck.core.result;

import com.lnatit.suck.core.KeySemantic;

public interface ConflictRisk {
    ConflictTag toTag();

    record StateSubset(KeySemantic parent, KeySemantic subset) implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("s_ss");
        }
    }

    record InterceptInput(KeySemantic interceptor) implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("i_ii");
        }
    }

    record RaceCondition() implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("i_rc");
        }
    }

    record IntentShare(boolean strict) implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("t_is");
        }
    }

    record DeferredRisk(boolean both) implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("r_dr");
        }
    }

    record LoseFocus() implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("r_lf");
        }
    }

    record InputBlock() implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return new ConflictTag.Simple("r_ib");
        }
    }
}

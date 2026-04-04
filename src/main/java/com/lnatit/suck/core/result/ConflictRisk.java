package com.lnatit.suck.core.result;

public non-sealed interface ConflictRisk extends ConflictInfo {
    ConflictTag toTag();

    default void attachTo(ConflictCollector collector) {
        collector.withRisk(this);
    }

    record StateSubset(boolean subjectIsSubset) implements ConflictRisk {
        @Override
        public ConflictTag toTag() {
            return ConflictTag.debug("s_ss");
        }
    }

    record InterceptInput(boolean subjectIsInterceptor) implements ConflictRisk {
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

    record IntentShare(boolean identical) implements ConflictRisk {
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

package com.lnatit.chord.result;

import com.lnatit.chord.eval.Modality;

// maybe we should split dynamic with informational?
// Do we have pure informational?
public abstract class DynamicRisk implements ConflictRisk
{
    private Severity severity;

    protected DynamicRisk(Severity severity) {
        this.severity = severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public Severity severity() {
        return this.severity;
    }

    public static final class StateSubset extends DynamicRisk
    {
        private final boolean subjectIsSubset;
        private boolean escalated = false;

        public StateSubset(boolean subjectIsSubset) {
            super(Severity.SAFE);
            this.subjectIsSubset = subjectIsSubset;
        }

        public boolean subjectIsSubset() {
            return subjectIsSubset;
        }

        @Override
        public ConflictTag tag() {
            return this.escalated ? ConflictTag.PARTIAL_OVERRIDE : ConflictTag.STATE_SUBSET;
        }

        public void escalate() {
            this.escalated = true;
        }
    }

    public static abstract class Interceptive extends DynamicRisk {
        protected Interceptive(Severity severity) {
            super(severity);
        }

        public void downgrade() {
            this.setSeverity(this.severity().downgrade());
        }
    }

    // 7.同资源独占不报CW.和RW》8.匹配的意图降级
    public static final class InterceptInput extends Interceptive
    {
        private final boolean subjectIsInterceptor;

        public InterceptInput(boolean subjectIsInterceptor) {
            super(Severity.WARNING);
            this.subjectIsInterceptor = subjectIsInterceptor;
        }

        public boolean subjectIsInterceptor() {
            return subjectIsInterceptor;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.INTERCEPT_INPUT;
        }
    }

    // 7.同资源独占不报CW.和RW》8.匹配的意图降级
    public static final class RaceCondition extends Interceptive
    {
        public RaceCondition() {
            super(Severity.SEVERE);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.RACE_CONDITION;
        }
    }

    public static abstract class ModalJudged extends DynamicRisk
    {
        protected ModalJudged(Severity severity) {
            super(severity);
        }

        public abstract void acceptModality(Modality subject, Modality opponent);
    }

    public static abstract class SingleModifier extends ModalJudged
    {
        private final boolean subjectIsModifier;

        protected SingleModifier(boolean subjectIsModifier) {
            super(Severity.SAFE);
            this.subjectIsModifier = subjectIsModifier;
        }

        public boolean subjectIsModifier() {
            return subjectIsModifier;
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            Modality modifier = this.subjectIsModifier() ? subject : opponent;
            switch (modifier) {
                case HOLD -> setSeverity(Severity.SAFE);
                case TOGGLE -> setSeverity(Severity.INFO);
                case CYCLE -> setSeverity(Severity.WARNING);
                case PRESS -> setSeverity(Severity.SEVERE);
            }
        }
    }

    /**
     * NONE + KEY
     * <p>
     * KEY Modality |   Severity
     * -------------+-------------
     * HOLD    |   SAFE
     * TOGGLE  |   INFO
     * CYCLE   |   WARNING
     * PRESS   |   SEVERE
     */
    public static final class ContextLeak extends SingleModifier
    {
        public ContextLeak(boolean subjectIsModifier) {
            super(subjectIsModifier);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.CONTEXT_LEAK;
        }
    }

    /**
     * KEY + KEY
     * <p>
     * Modal   |   Severity
     * -------------+-------------
     * HOLD + HOLD |   INFO
     * HOLD + *    |   WARNING
     * TOGGLE + T  |   WARNING
     * Others  |   SEVERE
     */
    public static final class DeferredRisk extends ModalJudged
    {
        public DeferredRisk() {
            super(Severity.INFO);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.DEFERRED_RISK;
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            if (subject == Modality.HOLD && opponent == Modality.HOLD) {
                setSeverity(Severity.INFO);
            }
            else if (subject == Modality.HOLD
                     || opponent == Modality.HOLD
                     || subject == Modality.TOGGLE && opponent == Modality.TOGGLE) {
                setSeverity(Severity.WARNING);
            } else {
                setSeverity(Severity.SEVERE);
            }
        }
    }

    /**
     * NONE + MOUSE
     * <p>
     * MOUSE Modal  |   Severity
     * -------------+-------------
     * HOLD    |   SAFE
     * TOGGLE  |   INFO
     * CYCLE   |   WARNING
     * PRESS   |   SEVERE
     */
    public static final class LoseFocus extends SingleModifier
    {
        public LoseFocus(boolean subjectIsModifier) {
            super(subjectIsModifier);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.LOSE_FOCUS;
        }
    }

    /**
     * KEY + MOUSE
     * <p>
     * Modal   |   Severity
     * -------------+-------------
     * HOLD + HOLD |   WARNING
     * Others  |   SEVERE
     */
    public static final class InputBlock extends ModalJudged
    {
        public InputBlock() {
            super(Severity.WARNING);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.INPUT_BLOCK;
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            if (subject == Modality.HOLD && opponent == Modality.HOLD) {
                setSeverity(Severity.WARNING);
            }
            else {
                setSeverity(Severity.SEVERE);
            }
        }
    }
}

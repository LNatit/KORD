package com.lnatit.chord.result;

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

    public static abstract class Escalatable extends DynamicRisk
    {
        private boolean escalated = false;

        protected Escalatable(Severity severity) {
            super(severity);
        }

        public void escalate() {
            this.escalated = true;
        }
    }

    public static final class StateSubset extends DynamicRisk
    {
        private final boolean subjectIsSubset;

        public StateSubset(boolean subjectIsSubset) {
            super(Severity.SAFE);
            this.subjectIsSubset = subjectIsSubset;
        }

        public boolean subjectIsSubset() {
            return subjectIsSubset;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.STATE_SUBSET;
        }
    }

    public static final class InterceptInput extends DynamicRisk
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

    public static final class RaceCondition extends DynamicRisk
    {
        public RaceCondition() {
            super(Severity.SEVERE);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.RACE_CONDITION;
        }
    }

    public static final class IntentShare extends DynamicRisk
    {
        private final boolean identical;

        public IntentShare(boolean identical) {
            super(Severity.SAFE);
            this.identical = identical;
        }

        public boolean identical() {
            return identical;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.INTENT_SHARE;
        }
    }

    /**
     * NONE + KEY
     * <p>
     * KEY Modality |   Severity
     * -------------+-------------
     *      HOLD    |   SAFE
     *      TOGGLE  |   INFO
     *      CYCLE   |   WARNING
     *      PRESS   |   SEVERE
     */
    public static final class ContextLeak extends DynamicRisk
    {
        private final boolean subjectIsModifier;

        public ContextLeak(boolean subjectIsModifier) {
            super(Severity.SAFE);
            this.subjectIsModifier = subjectIsModifier;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.CONTEXT_LEAK;
        }
    }

    /**
     * KEY + KEY
     * <p>
     *      Modal   |   Severity
     * -------------+-------------
     *  HOLD + HOLD |   INFO
     *  HOLD + *    |   WARNING
     *  TOGGLE + T  |   WARNING
     *      Others  |   SEVERE
     */
    public static final class DeferredRisk extends DynamicRisk
    {
        public DeferredRisk() {
            super(Severity.INFO);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.DEFERRED_RISK;
        }
    }

    /**
     * NONE + MOUSE
     * <p>
     * MOUSE Modal  |   Severity
     * -------------+-------------
     *      HOLD    |   SAFE
     *      TOGGLE  |   INFO
     *      CYCLE   |   WARNING
     *      PRESS   |   SEVERE
     */
    public static final class LoseFocus extends DynamicRisk
    {
        private final boolean subjectIsModifier;

        public LoseFocus(boolean subjectIsModifier) {
            super(Severity.SAFE);
            this.subjectIsModifier = subjectIsModifier;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.LOSE_FOCUS;
        }
    }

    /**
     * KEY + MOUSE
     * <p>
     *      Modal   |   Severity
     * -------------+-------------
     *  HOLD + HOLD |   WARNING
     *      Others  |   SEVERE
     */
    public static final class InputBlock extends DynamicRisk
    {
        public InputBlock() {
            super(Severity.SAFE);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.INPUT_BLOCK;
        }
    }
}

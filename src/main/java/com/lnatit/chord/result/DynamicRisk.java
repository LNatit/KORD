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
            super(Severity.SAFE);
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
            super(Severity.SAFE);
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

    public static final class DeferredRisk extends DynamicRisk
    {
        private final boolean both;

        public DeferredRisk(boolean both) {
            super(Severity.SAFE);
            this.both = both;
        }

        public boolean both() {
            return both;
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.DEFERRED_RISK;
        }
    }

    public static final class LoseFocus extends DynamicRisk
    {
        public LoseFocus() {
            super(Severity.SAFE);
        }

        @Override
        public ConflictTag tag() {
            return ConflictTag.LOSE_FOCUS;
        }
    }

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

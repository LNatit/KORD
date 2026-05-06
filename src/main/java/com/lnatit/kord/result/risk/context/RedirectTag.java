package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.eval.Modality;
import com.lnatit.kord.result.risk.RiskEntry;
import com.lnatit.kord.result.risk.RiskTag;
import com.lnatit.kord.result.risk.Severity;

public enum RedirectTag implements RiskTag
{
    NO_REDIRECT,
    CONTEXT_LEAK,
    DEFERRED_RISK,
    LOSE_FOCUS,
    FOCUS_COLLISION,
    INPUT_BLOCK,
    CONTEXT_CLASH;

    public interface ModalDependent {
        void acceptModality(Modality subject, Modality opponent);
    }

    private abstract static class SingleModifier extends RiskEntry.Simple<RedirectTag> implements ModalDependent {
        private final boolean leftIsModifier;

        protected SingleModifier(RedirectTag tag, boolean leftIsModifier) {
            super(tag);
            this.leftIsModifier = leftIsModifier;
            this.setSeverity(Severity.SAFE);
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            Modality modifier = leftIsModifier ? subject : opponent;
            switch (modifier) {
                case HOLD -> setSeverity(Severity.SAFE);
                case TOGGLE -> setSeverity(Severity.INFO);
                case CYCLE -> setSeverity(Severity.WARNING);
                case PRESS -> setSeverity(Severity.SEVERE);
            }
        }
    }

    public static final class ContextLeak extends SingleModifier {
        public ContextLeak(boolean subjectIsModifier) {
            super(CONTEXT_LEAK, subjectIsModifier);
        }
    }

    public static final class LoseFocus extends SingleModifier {
        public LoseFocus(boolean subjectIsModifier) {
            super(LOSE_FOCUS, subjectIsModifier);
        }
    }

    public static final class DeferredRisk extends RiskEntry.Simple<RedirectTag> implements ModalDependent {
        public DeferredRisk() {
            super(DEFERRED_RISK);
            this.setSeverity(Severity.INFO);
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            if (subject == Modality.HOLD && opponent == Modality.HOLD) {
                setSeverity(Severity.INFO);
            } else if (subject == Modality.HOLD
                    || opponent == Modality.HOLD
                    || subject == Modality.TOGGLE && opponent == Modality.TOGGLE) {
                setSeverity(Severity.WARNING);
            } else {
                setSeverity(Severity.SEVERE);
            }
        }
    }

    public static final class InputBlock extends RiskEntry.Simple<RedirectTag> implements ModalDependent {
        public InputBlock() {
            super(INPUT_BLOCK);
            this.setSeverity(Severity.WARNING);
        }

        @Override
        public void acceptModality(Modality subject, Modality opponent) {
            if (subject == Modality.HOLD && opponent == Modality.HOLD) {
                setSeverity(Severity.WARNING);
            } else {
                setSeverity(Severity.SEVERE);
            }
        }
    }
}

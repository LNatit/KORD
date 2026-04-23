package com.lnatit.chord.result;

import com.lnatit.chord.result.context.RiskTag;

public interface RiskEntry extends ConflictRisk
{
    RiskTag tag();

    record Diagnostic(RiskTag tag) implements RiskEntry
    {
        @Override
        public Severity severity() {
            return Severity.SAFE;
        }
    }

    class Simple<T extends RiskTag> implements RiskEntry
    {
        private final T tag;

        private Severity severity = Severity.SAFE;

        public Simple(T tag) {this.tag = tag;}


        @Override
        public RiskTag tag() {
            return this.tag;
        }

        @Override
        public Severity severity() {
            return this.severity;
        }

        public void setSeverity(Severity severity) {
            this.severity = severity;
        }
    }
}

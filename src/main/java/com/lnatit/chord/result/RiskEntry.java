package com.lnatit.chord.result;

public interface RiskEntry<T extends RiskTag> extends ConflictRisk
{
    T tag();

    static <T extends RiskTag> RiskEntry<T> create(T tag, Severity severity) {
        return new Immutable<>(tag, severity);
    }

    static <T extends RiskTag> RiskEntry<T> diagnostic(T tag) {
        return create(tag, Severity.SAFE);
    }

    record Immutable<T extends RiskTag>(T tag, Severity severity) implements RiskEntry<T>
    {
        @Override
        public Severity severity() {
            return this.severity;
        }
    }

    class Simple<T extends RiskTag> implements RiskEntry<T>
    {
        private final T tag;

        private Severity severity = Severity.SAFE;

        public Simple(T tag) {this.tag = tag;}

        @Override
        public T tag() {
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

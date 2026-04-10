package com.lnatit.suck.core.result;

public interface ConflictTag
{
    String shortCode();

    Severity severity();

    default boolean isDiagnostic() {
        return false;
    }

    static ConflictTag debug(String shortCode) {
        return new Debug(shortCode);
    }

    static ConflictTag simple(String shortCode, Severity severity) {
        return new Simple(shortCode, severity);
    }

    record Debug(String shortCode) implements ConflictTag
    {
        @Override
        public Severity severity() {
            return Severity.INFO;
        }

        @Override
        public boolean isDiagnostic() {
            return true;
        }
    }

    record Simple(String shortCode, Severity severity) implements ConflictTag
    {}


    record Pair(ConflictTag tag,boolean meltDown) implements ConflictInfo {
        public Pair(ConflictTag tag) {
            this(tag, false);
        }

        @Override
        public void attachTo(ConflictCollector collector) {
            collector.withTag(tag);
            if (meltDown) {
                collector.setFinished();
            }
        }
    }
}

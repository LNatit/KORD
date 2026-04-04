package com.lnatit.suck.core.result;

public interface ConflictTag
{
    String shortCode();

    default boolean isDiagnostic() {
        return false;
    }

    static ConflictTag debug(String shortCode) {
        return new Debug(shortCode);
    }

    static ConflictTag simple(String shortCode) {
        return new Simple(shortCode);
    }

    record Debug(String shortCode) implements ConflictTag
    {
        @Override
        public boolean isDiagnostic() {
            return true;
        }
    }

    record Simple(String shortCode) implements ConflictTag
    {}


    record Pair(ConflictTag tag, Severity severity, boolean meltDown) implements ConflictInfo {
        public Pair(ConflictTag tag, Severity severity) {
            this(tag, severity, false);
        }

        @Override
        public void attachTo(ConflictCollector collector) {
            collector.withTag(tag, severity);
            if (meltDown) {
                collector.setFinished();
            }
        }
    }
}

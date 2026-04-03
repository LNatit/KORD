package com.lnatit.suck.core.result;

public interface ConflictTag
{
    String shortCode();

    default boolean isDiagnostic() {
        return false;
    }

    default Pair withSeverity(Severity severity) {
        return new Pair(this, severity);
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


    record Pair(ConflictTag tag, Severity severity)
    {}
}

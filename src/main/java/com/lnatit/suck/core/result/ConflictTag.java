package com.lnatit.suck.core.result;

public interface ConflictTag
{
    String id();

    default boolean isDiagnostic() {
        return false;
    }

    default Pair withSeverity(Severity severity) {
        return new Pair(this, severity);
    }

    static ConflictTag debug(String id) {
        return new Debug(id);
    }

    static ConflictTag simple(String id) {
        return new Simple(id);
    }

    record Debug(String id) implements ConflictTag
    {
        @Override
        public boolean isDiagnostic() {
            return true;
        }
    }

    record Simple(String id) implements ConflictTag
    {}


    record Pair(ConflictTag tag, Severity severity)
    {}
}

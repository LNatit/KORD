package com.lnatit.chord.result;

import com.lnatit.chord.util.Supplier;

public interface ConflictRisk
{
    ConflictTag tag();

    Severity severity();

    static ConflictRisk create(ConflictTag tag, Severity severity) {
        return of(tag, severity);
    }

    static Static of(ConflictTag tag, Severity severity) {
        return new Static(tag, severity);
    }

    record Static(ConflictTag tag, Severity severity) implements ConflictRisk, Supplier<ConflictRisk>
    {
        @Override
        public ConflictRisk get() {
            return this;
        }
    }
}

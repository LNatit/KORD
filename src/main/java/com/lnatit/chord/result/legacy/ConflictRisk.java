package com.lnatit.chord.result.legacy;

import com.lnatit.chord.result.ConflictTag;
import com.lnatit.chord.result.Severity;
import com.lnatit.chord.util.Supplier;

public sealed interface ConflictRisk extends Severity.Supplier permits ConflictRisk.Static, DynamicRisk
{
    ConflictTag tag();

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

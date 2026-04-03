package com.lnatit.suck.core.result;

public class ConflictResult
{
    public static ConflictResult SAFE;

    // TODO give some params
    public static ConflictResult intentShared() {
        return null;
    }

    public ConflictResult reverse() {
        return this;
    }


    public enum Severity
    {
        SAFE, INFO, WARNING, SEVERE
    }

    public static class Builder
    {


        public Builder withTag(ConflictTag tag, Severity severity) {
            return this;
        }

        public ConflictResult build() {
            return SAFE;
        }
    }
}

package com.lnatit.kord.result.risk;

public enum Severity
{
    SAFE, INFO, WARNING, SEVERE;
    
    public Severity downgrade() {
        return switch (this) {
            case WARNING -> INFO;
            case SEVERE -> WARNING;
            default -> SAFE;
        };
    }

    public interface Supplier {
        Severity severity();
    }
}

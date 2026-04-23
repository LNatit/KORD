package com.lnatit.chord.result.context;

import com.lnatit.chord.result.RiskEntry;

public enum StateTag implements RiskTag
{
    STATE_MUTEX,
    STATE_INTERSECT,
    STATE_SUBSET;

    public static final RiskEntry STATE_MUTEX_RISK = new RiskEntry.Diagnostic(STATE_MUTEX);
    public static final RiskEntry STATE_INTERSECT_RISK = new RiskEntry.Diagnostic(STATE_INTERSECT);
    public static final RiskEntry STATE_SUBSET_RISK = new RiskEntry.Diagnostic(STATE_SUBSET);

    public RiskEntry toRisk() {
        return switch (this) {
            case STATE_MUTEX -> STATE_MUTEX_RISK;
            case STATE_INTERSECT -> STATE_INTERSECT_RISK;
            case STATE_SUBSET -> STATE_SUBSET_RISK;
        };
    }
}

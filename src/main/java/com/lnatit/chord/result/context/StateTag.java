package com.lnatit.chord.result.context;

import com.lnatit.chord.result.RiskEntry;
import com.lnatit.chord.result.RiskTag;
import com.lnatit.chord.result.Severity;

public enum StateTag implements RiskTag
{
    STATE_MUTEX,
    STATE_INTERSECT,
    STATE_SUBSET;

    public static final RiskEntry<StateTag> STATE_MUTEX_RISK = RiskEntry.diagnostic(STATE_MUTEX);
    public static final RiskEntry<StateTag> STATE_INTERSECT_RISK = RiskEntry.diagnostic(STATE_INTERSECT);
    public static final RiskEntry<StateTag> STATE_SUBSET_RISK = RiskEntry.diagnostic(STATE_SUBSET);

    public record StateSubset(boolean leftIsSubset) implements RiskEntry<StateTag>
    {
        @Override
        public StateTag tag() {
            return STATE_SUBSET;
        }

        @Override
        public Severity severity() {
            return Severity.SAFE;
        }
    }
}

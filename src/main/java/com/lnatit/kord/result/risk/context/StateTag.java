package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskEntry;
import com.lnatit.kord.result.risk.RiskTag;
import com.lnatit.kord.result.risk.Severity;

public enum StateTag implements RiskTag
{
    STATE_MUTEX("s_state_mutex"),
    STATE_INTERSECT("s_state_intersect"),
    STATE_SUBSET("s_state_subset");

    private final String id;

    StateTag(String id) {this.id = id;}

    @Override
    public String id() {
        return this.id;
    }

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

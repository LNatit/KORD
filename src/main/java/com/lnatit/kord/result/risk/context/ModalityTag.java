package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum ModalityTag implements RiskTag
{
    OPERATION_MATCH("m_operation_match"),
    TIMING_MISMATCH("m_timimng_mismatch"),
    REPEAT_SWITCH("m_repeat_switch"),
    STATE_DEADLOCK("m_state_lock"),
    STATE_EXPLODE("m_state_explode");

    private final String id;

    ModalityTag(String id) {this.id = id;}

    @Override
    public String id() {
        return this.id;
    }
}

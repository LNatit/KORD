package com.lnatit.kord.result.risk.context;

import com.lnatit.kord.result.risk.RiskTag;

public enum ModalityTag implements RiskTag
{
    OPERATION_MATCH,
    TIMING_MISMATCH,
    REPEAT_SWITCH,
    STATE_DEADLOCK,
    STATE_EXPLODE
}

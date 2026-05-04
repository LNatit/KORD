package com.lnatit.chord.result.risk.context;

import com.lnatit.chord.result.risk.RiskTag;

public enum ModalityTag implements RiskTag
{
    OPERATION_MATCH,
    TIMING_MISMATCH,
    REPEAT_SWITCH,
    STATE_DEADLOCK,
    STATE_EXPLODE
}

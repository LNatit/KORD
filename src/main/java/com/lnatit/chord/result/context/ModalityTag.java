package com.lnatit.chord.result.context;

import com.lnatit.chord.result.RiskTag;

public enum ModalityTag implements RiskTag
{
    OPERATION_MATCH,
    TIMING_MISMATCH,
    REPEAT_SWITCH,
    STATE_DEADLOCK,
    STATE_EXPLODE
}

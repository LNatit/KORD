package com.lnatit.chord.result.context;

public enum ModalityTag implements RiskTag
{
    OPERATION_MATCH,
    TIMING_MISMATCH,
    REPEAT_SWITCH,
    STATE_DEADLOCK,
    STATE_EXPLODE
}

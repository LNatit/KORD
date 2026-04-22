package com.lnatit.chord.result.context;

public enum RedirectTag implements RiskTag
{
    NO_REDIRECT,
    CONTEXT_LEAK,
    DEFERRED_RISK,
    LOSE_FOCUS,
    FOCUS_COLLISION,
    INPUT_BLOCK,
    CONTEXT_CLASH
}

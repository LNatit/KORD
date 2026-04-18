package com.lnatit.chord.result;

public record ConflictTag(String shortCode, boolean isDiagnostic)
{
    // K physical key
    public static final ConflictTag HARDWARE_MISMATCH = ConflictTag.debug("k_hm");

    // C context routing
    public static final ConflictTag CONTEXT_ROUTED = ConflictTag.debug("c_cr");

    // S state mutex
    public static final ConflictTag STATE_MUTEX = ConflictTag.debug("s_sm");
    public static final ConflictTag STATE_SUBSET = ConflictTag.debug("s_ss");

    // I input interception
    public static final ConflictTag INTERCEPT_INPUT = ConflictTag.simple("i_ii");
    public static final ConflictTag RACE_CONDITION = ConflictTag.simple("i_rc");
    public static final ConflictTag PARTIAL_OVERRIDE = ConflictTag.simple("i_po");
    public static final ConflictTag CONCURRENT_INPUT = ConflictTag.debug("i_ci");

    // R redirect mode
    public static final ConflictTag NO_REDIRECT = ConflictTag.simple("r_nr");
    public static final ConflictTag CONTEXT_LEAK = ConflictTag.simple("l_cl");
    public static final ConflictTag DEFERRED_RISK = ConflictTag.simple("r_dr");
    public static final ConflictTag LOSE_FOCUS = ConflictTag.simple("r_lf");
    public static final ConflictTag FOCUS_COLLISION = ConflictTag.simple("r_fc");
    public static final ConflictTag INPUT_BLOCK = ConflictTag.simple("r_ib");
    public static final ConflictTag CONTEXT_CLASH = ConflictTag.simple("r_cc");

    // E resource access
    public static final ConflictTag RESOURCE_MUTEX = ConflictTag.debug("e_rm");
    public static final ConflictTag CONCURRENT_ACCESS = ConflictTag.debug("e_ca");
    public static final ConflictTag CONCURRENT_WRITE = ConflictTag.simple("e_cw");
    public static final ConflictTag READ_WRITE = ConflictTag.simple("e_rw");

    // T player intent
    public static final ConflictTag INTENT_SHARE = ConflictTag.simple("t_is");
    @Deprecated
    public static final ConflictTag INTENT_SHARED = ConflictTag.simple("t_ii");

    // M operation modality
    public static final ConflictTag OPERATION_MATCH = ConflictTag.simple("m_om");
    public static final ConflictTag TIMING_MISMATCH = ConflictTag.simple("m_tm");
    public static final ConflictTag REPEAT_SWITCH = ConflictTag.simple("m_rs");
    public static final ConflictTag STATE_LOCK = ConflictTag.simple("m_sl");
    public static final ConflictTag STATE_EXPLODE = ConflictTag.simple("m_se");

    // U override source
    public static final ConflictTag USER_OVERRIDE = ConflictTag.debug("u_uo");
    public static final ConflictTag BUILTIN_OVERRIDE = ConflictTag.debug("u_bo");
    public static final ConflictTag CREATOR_OVERRIDE = ConflictTag.debug("u_co");
    public static final ConflictTag PLAYER_OVERRIDE = ConflictTag.debug("u_po");

    static ConflictTag debug(String shortCode) {
        return new ConflictTag(shortCode, true);
    }

    static ConflictTag simple(String shortCode) {
        return new ConflictTag(shortCode, false);
    }
}

package com.lnatit.kord.semantic;

public enum ConflictType
{
    /** Always-False: 冲突判定恒为 false，短路 SAFE */
    NEVER,
    /** Self-Limited: 仅与自身冲突，支持语义指定，进入语义流水线 */
    SELF_ONLY,
    /** Global/Complex: 复杂或全局冲突条件，走原始 evalStatic() 直判，不进语义流水线 */
    CUSTOM
}

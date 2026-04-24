package com.lnatit.chord.eval;

import com.lnatit.chord.result.RiskEntry;
import com.lnatit.chord.result.Severity;
import com.lnatit.chord.result.context.ModalityTag;
import com.lnatit.chord.util.AsymmetricEnumMatrix;

/**
 * <h3>模态定义缩写：</h3>
 * <ul>
 *     <li><b>P</b> (PRESS): 无状态，单点瞬发</li>
 *     <li><b>H</b> (HOLD): 无状态，长按持续</li>
 *     <li><b>T</b> (TOGGLE): 有状态，二元切换</li>
 *     <li><b>C</b> (CYCLE): 有状态，多元轮转</li>
 * </ul>
 *
 * <h3>冲突分类与严重度对照表：</h3>
 * <table border="1" cellpadding="5" cellspacing="0">
 *     <tr>
 *         <th>组合 (A x B)</th>
 *         <th>分类代号</th>
 *         <th>分类全称 (Category)</th>
 *         <th>严重等级 (Severity)</th>
 *         <th>核心判定依据 (复原成本)</th>
 *     </tr>
 *     <tr>
 *         <td>PP, HH</td>
 *         <td><b>OM</b></td>
 *         <td>Operation Match (操作契合)</td>
 *         <td><font color="green">SAFE</font></td>
 *         <td>物理与逻辑完全同频，无错位。</td>
 *     </tr>
 *     <tr>
 *         <td>PH, HT</td>
 *         <td><b>TM</b></td>
 *         <td>Timing Mismatch (时序不符)</td>
 *         <td><font color="blue">INFO</font></td>
 *         <td>时序交叠，但状态复原仅需 1 次额外操作。</td>
 *     </tr>
 *     <tr>
 *         <td>HC</td>
 *         <td><b>TM</b></td>
 *         <td>Timing Mismatch (时序不符)</td>
 *         <td><font color="orange">WARNING</font></td>
 *         <td>Hold 连带多元循环，复原需 N-1 次（复数）操作。</td>
 *     </tr>
 *     <tr>
 *         <td>PT</td>
 *         <td><b>RS</b></td>
 *         <td>Repeat Switch (连带切换)</td>
 *         <td><font color="blue">INFO</font></td>
 *         <td>高频敲击连带二元翻转，复原仅需 1 次操作。</td>
 *     </tr>
 *     <tr>
 *         <td>PC</td>
 *         <td><b>RS</b></td>
 *         <td>Repeat Switch (连带切换)</td>
 *         <td><font color="orange">WARNING</font></td>
 *         <td>高频敲击连带多元轮转，极易导致状态迷失。</td>
 *     </tr>
 *     <tr>
 *         <td>TT</td>
 *         <td><b>SL</b></td>
 *         <td>State Lock (相位锁死)</td>
 *         <td><font color="orange">WARNING</font></td>
 *         <td>二元状态相斥，一旦错位将永久反相，无法通过单键对齐。</td>
 *     </tr>
 *     <tr>
 *         <td>TC, CC</td>
 *         <td><b>SE</b></td>
 *         <td>State Explode (状态爆炸)</td>
 *         <td><font color="red">SEVERE</font></td>
 *         <td>多周期状态机错位，复原需敲击最小公倍数(LCM)次，等同于瘫痪。</td>
 *     </tr>
 * </table>
 */
public enum Modality
{
    // We combine HOLD with RELEASE, cuz hardly you see a pure RELEASE key
    PRESS, HOLD, TOGGLE, CYCLE;

    public static final AsymmetricEnumMatrix<Modality, RiskEntry<ModalityTag>> MATRIX =
            new AsymmetricEnumMatrix<>(Modality.class, RiskEntry.create(ModalityTag.OPERATION_MATCH, Severity.SAFE));

    static {
        MATRIX.putAll(HOLD, RiskEntry.create(ModalityTag.TIMING_MISMATCH, Severity.INFO), PRESS, TOGGLE);
        MATRIX.put(PRESS, TOGGLE, RiskEntry.create(ModalityTag.REPEAT_SWITCH, Severity.INFO));
        MATRIX.put(HOLD, CYCLE, RiskEntry.create(ModalityTag.TIMING_MISMATCH, Severity.WARNING));
        MATRIX.put(PRESS, CYCLE, RiskEntry.create(ModalityTag.REPEAT_SWITCH, Severity.WARNING));
        MATRIX.put(TOGGLE, TOGGLE, RiskEntry.create(ModalityTag.STATE_DEADLOCK, Severity.WARNING));
        MATRIX.putAll(CYCLE, RiskEntry.create(ModalityTag.STATE_EXPLODE, Severity.SEVERE), TOGGLE, CYCLE);
    }
}

package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictResult;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public interface Evaluator
{
    /**
     * 评估两个按键绑定之间的冲突关系，并生成带有双向严重度和标签的综合判定结果。
     * <p>
     * 执行完整的 <b>8 层 Chord 语义判定管线</b>。管线分为两个主要阶段：
     * <ul>
     *     <li><b>阶段一：短路拦截区 (Early Exit Phase)</b> - 严格顺序执行，一旦符合豁免条件立即终止并返回。</li>
     *     <li><b>阶段二：并行打分区 (Parallel Scoring Phase)</b> - 无顺序依赖，综合评估劫持、模态与动作干涉，汇总得出最终结果。</li>
     * </ul>
     *
     * <pre>
     *  [Subject Key] & [Opponent Key]
     *                │
     *                ▼
     *  === PHASE 1: EARLY EXIT (Sequential) ===
     *   1. Hardware Match? ────(No)──&gt; [SAFE]
     *   2. Engine Context? ────(No)──&gt; [SAFE]
     *   3. User Override?  ───(Yes)──&gt; [Override Result]
     *   4. State Mutex?    ───(Yes)──&gt; [SAFE]
     *   5. Shared Intent?  ───(Yes)──&gt; [SAFE / INFO]
     *                │
     *                ▼
     *  === PHASE 2: SCORING (Parallel) ===
     *   6. Hijack Score   (Asymmetrical Override / Input Consume)
     *   7. Modality Score (Time-state Mismatch)
     *   8. Matrix Score   (Action Logic Collision)
     *                │
     *                ▼
     *             [MERGE]
     *  ConflictResult (Severity For Subject, Severity For Opponent, Tags)
     * </pre>
     * <p>
     * <b>注意：</b> 冲突判定具有方向性。例如，当 Subject 存在输入劫持（Consume）时，
     * Subject 自身可能功能正常（SAFE），而 Opponent 会被判定为瘫痪（SEVERE）。
     *
     * @param subject  must be bound
     * @param opponent
     * @return
     * @see KeyMapping#same(KeyMapping)
     */
    static ConflictResult eval(KeyMapping subject, KeyMapping opponent) {
        // Hardware input
        if (!isSameKey(subject, opponent)) {
            return ConflictResult.SAFE;
        }

        // Context routing
        if (!isContextOverlapping(subject, opponent)) {
            return ConflictResult.SAFE;
        }

        // User override
        ConflictResult override = getUserOverride(subject, opponent);
        if (override != null) {return override;}

        // State mutex
        if (isStateMutex(subject, opponent)) {
            return ConflictResult.SAFE;
        }

        KeySemantic subjectSemantic = ((SemanticalKey) subject).chord$getSemantic();
        KeySemantic opponentSemantic = ((SemanticalKey) opponent).chord$getSemantic();
        boolean advanced = canApplyAdvancedLogic(subjectSemantic, opponentSemantic);

        if (advanced) {
            assert subjectSemantic instanceof KeySemantic.Advanced;
            assert opponentSemantic instanceof KeySemantic.Advanced;
            // Shared intent
            if (Intent.hasShared(((KeySemantic.Advanced) subjectSemantic).intents(),
                                 ((KeySemantic.Advanced) opponentSemantic).intents())) {
                return ConflictResult.intentShared();
            }
        }

        ConflictResult.Builder builder = new ConflictResult.Builder();

        builder = evaluateHijack(subjectSemantic, opponentSemantic, builder);

        if (advanced) {
            assert subjectSemantic instanceof KeySemantic.Advanced;
            assert opponentSemantic instanceof KeySemantic.Advanced;
            builder = evaluateModality((KeySemantic.Advanced) subjectSemantic,
                                       (KeySemantic.Advanced) opponentSemantic,
                                       builder);

            builder = evaluateMatrix((KeySemantic.Advanced) subjectSemantic,
                                     (KeySemantic.Advanced) opponentSemantic,
                                     builder);
        }

        return builder.build();
    }

    static boolean isSameKey(KeyMapping subject, KeyMapping opponent) {
        return subject.getKey().equals(opponent.getKey())
               && subject.getDefaultKeyModifier()
                         .equals(opponent.getDefaultKeyModifier())
               && subject.getKeyModifier().equals(opponent.getKeyModifier());
    }

    static boolean isContextOverlapping(KeyMapping subject, KeyMapping opponent) {
        IKeyConflictContext subjectCtx = ((SemanticalKey) subject).semanticalConflictCtx();
        IKeyConflictContext opponentCtx = ((SemanticalKey) opponent).semanticalConflictCtx();
        return subjectCtx.conflicts(opponentCtx) || opponentCtx.conflicts(subjectCtx);
    }

    static ConflictResult getUserOverride(KeyMapping subject, KeyMapping opponent) {
        // TODO null represents no override
        return null;
    }

    static boolean isStateMutex(KeyMapping subject, KeyMapping opponent) {
        StateSet subjectStates = ((SemanticalKey) subject).chord$getSemantic().states();
        StateSet opponentStates = ((SemanticalKey) opponent).chord$getSemantic().states();
        return StateSet.isMutex(subjectStates, opponentStates);
    }

    static boolean canApplyAdvancedLogic(KeySemantic semantic1, KeySemantic semantic2) {
        boolean advanced = semantic1 instanceof KeySemantic.Advanced && semantic2 instanceof KeySemantic.Advanced;
        boolean same = semantic1.getClass().equals(semantic2.getClass());
        return advanced && same;
    }

    static ConflictResult.Builder evaluateHijack(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictResult.Builder builder
    ) {
        HijackMode subjectHijack = opponentSemantic.hijackMode();
        HijackMode opponentHijack = opponentSemantic.hijackMode();


        return builder;
    }

    static ConflictResult.Builder evaluateModality(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictResult.Builder builder
    ) {
        return builder;
    }

    static ConflictResult.Builder evaluateMatrix(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictResult.Builder builder
    ) {
        return builder;
    }
}

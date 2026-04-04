package com.lnatit.suck.core;

import com.lnatit.suck.core.result.*;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import javax.annotation.Nullable;
import java.util.List;

public interface Evaluator {
    /**
     * @param subject  must be bound
     * @param opponent
     * @return
     * @see KeyMapping#same(KeyMapping)
     */
    static ConflictResult eval(KeyMapping subject, KeyMapping opponent) {
        ConflictCollector collector = new ConflictCollector();

        // Physical Key
        if (!isSameKey(subject, opponent)) {
            // hardware_mismatch
            collector.withDebugTag("k_hm");
            return collector.toResult();
        }

        // Context routing
        if (!isContextOverlapping(subject, opponent)) {
            // context_routed
            collector.withDebugTag("c_cr");
            return collector.toResult();
        }

        // User override
        ConflictResult override = getUserOverride(subject, opponent);
        // TODO add debug tag user_override (u_*o), distinguish builtin(b) creator(c) user(u) player(p)
        if (override != null) {
            return override;
        }

        // State mutex
        // TODO add deferred tag
        if (isStateMutex(subject, opponent)) {
            // state_mutex
            collector.withDebugTag("s_sm");
            return collector.toResult();
        }

        KeySemantic subjectSemantic = ((SemanticalKey) subject).chord$getSemantic();
        KeySemantic opponentSemantic = ((SemanticalKey) opponent).chord$getSemantic();

        // Input interception
        evaluateIntercept(subjectSemantic, opponentSemantic, collector);
        if (collector.finished()) {
            return collector.toResult();
        }

        // Redirect mode
        evaluateRedirect(subjectSemantic, opponentSemantic, collector);
        if (collector.finished()) {
            return collector.toResult();
        }

        boolean advanced = canApplyAdvancedLogic(subjectSemantic, opponentSemantic);
        if (advanced) {
            assert subjectSemantic instanceof KeySemantic.Advanced;
            assert opponentSemantic instanceof KeySemantic.Advanced;

            // Player intent (use T instead of I to avoid confusion with intercept)
            evaluateIntent((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
            if (collector.finished()) {
                return collector.toResult();
            }

            collector.resolvePendingRisks();

            evaluateModality((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);

            evaluateCategory((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
        } else {
            collector.resolvePendingRisks();
        }

        return collector.toResult();
    }

    static boolean isSameKey(KeyMapping subject, KeyMapping opponent) {
        return subject.getKey().equals(opponent.getKey()) && subject.getDefaultKeyModifier().equals(opponent.getDefaultKeyModifier()) && subject.getKeyModifier().equals(opponent.getKeyModifier());
    }

    static boolean isContextOverlapping(KeyMapping subject, KeyMapping opponent) {
        IKeyConflictContext subjectCtx = ((SemanticalKey) subject).semanticalConflictCtx();
        IKeyConflictContext opponentCtx = ((SemanticalKey) opponent).semanticalConflictCtx();
        return subjectCtx.conflicts(opponentCtx) || opponentCtx.conflicts(subjectCtx);
    }

    @Nullable
    static ConflictResult getUserOverride(KeyMapping subject, KeyMapping opponent) {
        // TODO null represents no override
        return null;
    }

    static boolean isStateMutex(KeyMapping subject, KeyMapping opponent) {
        StateSet subjectStates = ((SemanticalKey) subject).chord$getSemantic().states();
        StateSet opponentStates = ((SemanticalKey) opponent).chord$getSemantic().states();
        return StateSet.isMutex(subjectStates, opponentStates);
    }

    static void evaluateIntercept(KeySemantic subjectSemantic, KeySemantic opponentSemantic, ConflictCollector collector) {
        // Hijack eval depends on other contexts, so we don't use matrix indexing...
        boolean si = subjectSemantic.intercept();
        boolean oi = opponentSemantic.intercept();

        if (si && oi) {
            // race_condition
            collector.withRisk(new ConflictRisk.RaceCondition());
            return;
        }

        if (si || oi) {
            // TODO check whether race condition need similar logic
            ConflictRisk.StateSubset risk = collector.getRisk(ConflictRisk.StateSubset.class);
            if (risk != null && risk.subjectIsSubset() == si) {
                // partial_override
                collector.withTag(risk.toTag());
                collector.withTag(new ConflictTag.Simple("i_po"), Severity.INFO);
                collector.setFinished();
                return;
            }

            // intercept_input
            collector.withRisk(new ConflictRisk.InterceptInput(si));
            return;
        }

        // concurrent_input
        collector.withDebugTag("h_ci");
    }

    static void evaluateRedirect(KeySemantic subjectSemantic, KeySemantic opponentSemantic, ConflictCollector collector) {
        ConflictInfo info = RedirectMode.MATRIX.get(subjectSemantic.redirectMode(), opponentSemantic.redirectMode());
        info.attachTo(collector);
    }

    static boolean canApplyAdvancedLogic(KeySemantic semantic1, KeySemantic semantic2) {
        boolean advanced = semantic1 instanceof KeySemantic.Advanced && semantic2 instanceof KeySemantic.Advanced;
        boolean same = semantic1.getClass().equals(semantic2.getClass());
        return advanced && same;
    }

    static void evaluateIntent(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector collector) {
        List<String> sI = subjectSemantic.intents();
        List<String> oI = opponentSemantic.intents();
        if (Intent.hasShared(sI, oI)) {
            ConflictRisk.InterceptInput risk = collector.getRisk(ConflictRisk.InterceptInput.class);
            if (risk != null) {
                // intent_shared
                collector.withTag(risk.toTag());
                collector.withTag(new ConflictTag.Simple("t_ii"), Severity.INFO);
                collector.setFinished();
                return;
            }

            // intent_shared
            collector.withRisk(new ConflictRisk.IntentShare(Intent.isIdentical(sI, oI)));
        }
    }

    static void evaluateModality(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector collector) {
        collector.withPair(Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()));
    }

    static void evaluateCategory(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector collector) {
        assert subjectSemantic.getClass() == opponentSemantic.getClass();
        if (subjectSemantic instanceof KeySemantic.InGame) {
            collector.withPair(ActionRoot.InGame.MATRIX.get((ActionRoot.InGame) subjectSemantic.actionRoot(), (ActionRoot.InGame) opponentSemantic.actionRoot()));
        } else if (subjectSemantic instanceof KeySemantic.InGui) {
            collector.withPair(ActionRoot.InGui.MATRIX.get((ActionRoot.InGui) subjectSemantic.actionRoot(), (ActionRoot.InGui) opponentSemantic.actionRoot()));
        }
    }
}

package com.lnatit.suck.core;

import com.lnatit.suck.core.result.*;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import javax.annotation.Nullable;

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
        boolean advanced = canApplyAdvancedLogic(subjectSemantic, opponentSemantic);

        if (advanced) {
            assert subjectSemantic instanceof KeySemantic.Advanced;
            assert opponentSemantic instanceof KeySemantic.Advanced;

            // Shared intent (use T instead of I to avoid confusion with intercept)
            evaluateIntent((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
            if (collector.finished()) {
                return collector.toResult();
            }

            evaluateModality((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);

            evaluateCategory((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
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

    static void evaluateIntercept(KeySemantic subjectSemantic, KeySemantic opponentSemantic, ConflictCollector builder) {
        // Hijack eval depends on other contexts, so we don't use matrix indexing...
        boolean si = subjectSemantic.intercept();
        boolean oi = opponentSemantic.intercept();

        if (si && oi) {
            // race_condition
            builder.withRisk(new ConflictRisk.RaceCondition());
            return;
        }

        if (si || oi) {
            // intercept_input
            builder.withRisk(new ConflictRisk.InterceptInput(si ? subjectSemantic : opponentSemantic));
            return;
        }

        // concurrent_input
        builder.withDebugTag("h_ci");
    }

    static void evaluateRedirect(KeySemantic subjectSemantic, KeySemantic opponentSemantic, ConflictCollector builder) {
        RedirectMode sR = subjectSemantic.redirectMode();
        RedirectMode oR = opponentSemantic.redirectMode();


    }

    static boolean canApplyAdvancedLogic(KeySemantic semantic1, KeySemantic semantic2) {
        boolean advanced = semantic1 instanceof KeySemantic.Advanced && semantic2 instanceof KeySemantic.Advanced;
        boolean same = semantic1.getClass().equals(semantic2.getClass());
        return advanced && same;
    }

    static void evaluateIntent(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector builder) {
        if (Intent.hasShared(subjectSemantic.intents(), opponentSemantic.intents())) {
            // intent_shared
            builder.withDebugTag("i_is");

            HijackMode sm = subjectSemantic.intercept();
            HijackMode om = opponentSemantic.intercept();
            // Redirect needs further diagnose
            if (!sm.isRedirect() && !om.isRedirect()) {
                builder.blockPipeline();
            }
        }
    }

    static void evaluateModality(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector builder) {
        builder.withPair(Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()));
    }

    static void evaluateCategory(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector builder) {
        assert subjectSemantic.getClass() == opponentSemantic.getClass();
        if (subjectSemantic instanceof KeySemantic.InGame) {
            builder.withPair(ActionRoot.InGame.MATRIX.get((ActionRoot.InGame) subjectSemantic.actionRoot(), (ActionRoot.InGame) opponentSemantic.actionRoot()));
        } else if (subjectSemantic instanceof KeySemantic.InGui) {
            builder.withPair(ActionRoot.InGui.MATRIX.get((ActionRoot.InGui) subjectSemantic.actionRoot(), (ActionRoot.InGui) opponentSemantic.actionRoot()));
        }
    }
}

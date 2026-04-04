package com.lnatit.suck.core;

import com.lnatit.suck.core.result.ConflictCollector;
import com.lnatit.suck.core.result.ConflictResult;
import com.lnatit.suck.core.result.ConflictTag;
import com.lnatit.suck.core.result.Severity;
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

        evaluateIntercept(subjectSemantic, opponentSemantic, collector);
        if (collector.isBlocked()) {
            return collector.toResult();
        }
        boolean advanced = canApplyAdvancedLogic(subjectSemantic, opponentSemantic);

        if (advanced) {
            assert subjectSemantic instanceof KeySemantic.Advanced;
            assert opponentSemantic instanceof KeySemantic.Advanced;
            // Shared intent
            evaluateIntent((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
            if (collector.isBlocked()) {
                return collector.toResult();
            }

            evaluateModality((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);

            evaluateMatrix((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
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

        if (si == HijackMode.PASSIVE && oi == HijackMode.PASSIVE) {
            // concurrent_input
            builder.withDebugTag("h_ci");
            return;
        }

        boolean sf = si == HijackMode.CONSUME;
        boolean of = oi == HijackMode.CONSUME;
        if (sf || of) {
            if (sf && of) {
                // risk_condition
                builder.withTag(ConflictTag.simple("h_rc"), Severity.SEVERE);
            } else {
                // input_interception
                builder.withTag(ConflictTag.simple("h_ii"), Severity.SEVERE);
            }
            builder.blockPipeline();
            return;
        }

        sf = si == HijackMode.REDIRECT_MOUSE;
        of = oi == HijackMode.REDIRECT_MOUSE;
        if (sf || of) {
            if (sf && of) {
                // focus_collision
                builder.withTag(ConflictTag.simple("h_fc"), Severity.SEVERE);
            } else {
                if (sf) {
                    if (oi == HijackMode.REDIRECT_KEY) {
                        // input_block
                        builder.withTag(ConflictTag.simple("h_ib"), Severity.SEVERE);
                    } else {
                        assert oi == HijackMode.PASSIVE;
                        // focus_interrupt
                        builder.withTag(ConflictTag.simple("h_fi"), Severity.WARNING);
                    }
                }
            }
        }


//        if (sm == HijackMode.REDIRECT_MOUSE && om == HijackMode.REDIRECT_MOUSE) {
//            builder.blockPipeline();
//        }
//
//        if (sm == HijackMode.REDIRECT_MOUSE && om == HijackMode.REDIRECT_KEY) {
//            //
//        }


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

    static void evaluateMatrix(KeySemantic.Advanced subjectSemantic, KeySemantic.Advanced opponentSemantic, ConflictCollector builder) {
        assert subjectSemantic.getClass() == opponentSemantic.getClass();
        if (subjectSemantic instanceof KeySemantic.InGame) {
            builder.withPair(ActionRoot.InGame.MATRIX.get((ActionRoot.InGame) subjectSemantic.actionRoot(), (ActionRoot.InGame) opponentSemantic.actionRoot()));
        } else if (subjectSemantic instanceof KeySemantic.InGui) {
            builder.withPair(ActionRoot.InGui.MATRIX.get((ActionRoot.InGui) subjectSemantic.actionRoot(), (ActionRoot.InGui) opponentSemantic.actionRoot()));
        }
    }
}

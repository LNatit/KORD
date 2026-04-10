package com.lnatit.suck.core;

import com.lnatit.suck.core.result.*;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Evaluator
{
    /**
     * @see KeyMapping#same(KeyMapping)
     */
    static ConflictResult conflicts(KeyMapping subject, KeyMapping opponent) {
        ConflictCollector collector = new ConflictCollector();

        // Physical Key
        if (!isSameKey(subject, opponent)) {
            // hardware_mismatch
            collector.withDebugTag("k_hm");
            return collector.toResult();
        }

        // User override
        Optional<ConflictResult> override = getUserOverride(subject, opponent);
        // TODO add debug tag user_override (u_*o), distinguish builtin(b) creator(c) user(u) player(p)
        if (override.isPresent()) {
            return override.get();
        }

        for (Map.Entry<IKeyConflictContext, KeySemantic> sEntry : ((SemanticalKey) subject).chord$getSemanticEntries()) {
            for (Map.Entry<IKeyConflictContext, KeySemantic> oEntry : ((SemanticalKey) opponent).chord$getSemanticEntries()) {
                // Context routing
                if (!isContextOverlapping(sEntry.getKey(), oEntry.getKey())) {
                    // context_routed
                    collector.withDebugTag("c_cr");
                    continue;
                }

                collector.merge(eval(sEntry.getValue(), oEntry.getValue()));
            }
        }

        return collector.toResult();
    }

    static boolean isSameKey(KeyMapping subject, KeyMapping opponent) {
        return subject.getKey().equals(opponent.getKey())
               && subject.getDefaultKeyModifier()
                         .equals(opponent.getDefaultKeyModifier())
               && subject.getKeyModifier().equals(opponent.getKeyModifier());
    }

    static boolean isContextOverlapping(IKeyConflictContext subjectContext, IKeyConflictContext opponentContext) {
        return subjectContext.conflicts(opponentContext) || opponentContext.conflicts(subjectContext);
    }

    static Optional<ConflictResult> getUserOverride(KeyMapping subject, KeyMapping opponent) {
        // TODO to impl
        return Optional.empty();
    }

    static ConflictCollector eval(KeySemantic subject, KeySemantic opponent) {
        ConflictCollector collector = new ConflictCollector();

        // State mutex
        // TODO add deferred tag
        if (isStateMutex(subject, opponent)) {
            // state_mutex
            collector.withDebugTag("s_sm");
            return collector;
        }

        // Input interception
        evaluateIntercept(subject, opponent, collector);
        if (collector.finished()) {
            return collector;
        }

        // Redirect mode
        evaluateRedirect(subject, opponent, collector);
        if (collector.finished()) {
            return collector;
        }

        boolean advanced = canApplyAdvancedLogic(subject, opponent);
        if (advanced) {
            assert subject instanceof KeySemantic.Advanced;
            assert opponent instanceof KeySemantic.Advanced;

            // Player intent (use T instead of I to avoid confusion with intercept)
            evaluateIntent((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);
            if (collector.finished()) {
                return collector;
            }

            evaluateModality((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);

//            evaluateCategory((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);
        }

        collector.resolvePendingRisks();
        return collector;
    }

    static boolean isStateMutex(KeySemantic subject, KeySemantic opponent) {
        StateSet subjectStates = subject.states();
        StateSet opponentStates = opponent.states();
        return StateSet.isMutex(subjectStates, opponentStates);
    }

    static void evaluateIntercept(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictCollector collector
    ) {
        // Hijack eval depends on other contexts, so we don't use matrix indexing...
        boolean si = subjectSemantic.intercept();
        boolean oi = opponentSemantic.intercept();
        if (si && oi && subjectSemantic.context() == KeyContext.IN_GAME) {
            // race_condition
            collector.withRisk(new ConflictRisk.RaceCondition());
            return;
        }

        if (si || oi) {
            // TODO check whether race condition need similar logic
            ConflictRisk.StateSubset risk = collector.getRisk(ConflictRisk.StateSubset.class);
            if (risk != null) {
                if (risk.subjectIsSubset() == si || risk.subjectIsSubset() == !oi) {
                    // partial_override
                    collector.withTag(risk.toTag());
                    collector.withTag("i_po", Severity.INFO);
                    collector.setFinished();
                    return;
                }
            }

            // intercept_input
            collector.withRisk(new ConflictRisk.InterceptInput(si));
            return;
        }

        // concurrent_input
        collector.withDebugTag("h_ci");
    }

    static void evaluateRedirect(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictCollector collector
    ) {
        ConflictInfo info = RedirectMode.MATRIX.get(subjectSemantic.redirectMode(), opponentSemantic.redirectMode());
        info.attachTo(collector);
    }

    static boolean canApplyAdvancedLogic(KeySemantic semantic1, KeySemantic semantic2) {
        return semantic1 instanceof KeySemantic.Advanced && semantic2 instanceof KeySemantic.Advanced;
    }

    static void evaluateIntent(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        List<String> sI = subjectSemantic.intents();
        List<String> oI = opponentSemantic.intents();
        if (Intent.hasShared(sI, oI)) {
            ConflictRisk.InterceptInput risk = collector.getRisk(ConflictRisk.InterceptInput.class);
            if (risk != null) {
                // intent_shared
                collector.withTag(risk.toTag());
                collector.withTag("t_ii", Severity.INFO);
                collector.setFinished();
                return;
            }

            // intent_shared
            collector.withRisk(new ConflictRisk.IntentShare(Intent.isIdentical(sI, oI)));
        }
    }

    static void evaluateModality(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        collector.withPair(Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()));
    }

    static void evaluateResource(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        Resource sRes = subjectSemantic.resource();
        Resource oRes = opponentSemantic.resource();
        if (Resource.overlaps(sRes, oRes)) {
            if (!(subjectSemantic.readOnly() && opponentSemantic.readOnly())) {
                if (!subjectSemantic.readOnly() && !opponentSemantic.readOnly()) {
                    if (!sRes.supportsConcurrentWrites) {
                        collector.withTag("r_cw", Severity.SEVERE);
                    }
                }
                else {
                    collector.withTag("r_rw", sRes.supportsConcurrentWrites ? Severity.INFO : Severity.WARNING);
                }
            }
            return;
        }
        // add debug tag?
        return;
    }
}

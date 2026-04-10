package com.lnatit.chord.core;

import com.lnatit.chord.core.result.*;
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
            collector.withDebug(ConflictTag.HARDWARE_MISMATCH);
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
                    collector.withDebug(ConflictTag.CONTEXT_ROUTED);
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
            collector.withDebug(ConflictTag.STATE_MUTEX);
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

        if (si || oi) {
            // TODO check whether race condition need similar logic
            DynamicRisk.StateSubset risk = collector.getRisk(DynamicRisk.StateSubset.class);
            if (risk != null && (risk.subjectIsSubset() == si || risk.subjectIsSubset() == !oi)) {
                // partial_override
                // TODO double check
//                    collector.withDebug(risk.tag());
                collector.withRisk(ConflictTag.PARTIAL_OVERRIDE, Severity.INFO);
                collector.setFinished();
                return;
            }

            if (si && oi) {
                // race_condition
                collector.withRisk(new DynamicRisk.RaceCondition());
                return;
            }

            // intercept_input
            collector.withRisk(new DynamicRisk.InterceptInput(si));
            return;
        }

        // concurrent_input
        collector.withDebug(ConflictTag.CONCURRENT_INPUT);
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
            DynamicRisk.InterceptInput risk = collector.getRisk(DynamicRisk.InterceptInput.class);
            if (risk != null) {
                // intent_shared
                // TODO double check
//                collector.withDebug(risk.tag());
//                collector.withRisk(ConflictTag.INTENT_SHARED, Severity.INFO);
                risk.setSeverity(Severity.INFO);
                collector.setFinished();
                return;
            }

            // intent_shared
            collector.withRisk(new DynamicRisk.IntentShare(Intent.isIdentical(sI, oI)));
        }
    }

    static void evaluateModality(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()).attachTo(collector);
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
                        collector.withRisk(ConflictTag.CONCURRENT_WRITE, Severity.SEVERE);
                    }
                }
                else {
                    collector.withRisk(ConflictTag.READ_WRITE,
                                       sRes.supportsConcurrentWrites ? Severity.INFO : Severity.WARNING);
                }
            }
            return;
        }
        // add debug tag?
        return;
    }
}

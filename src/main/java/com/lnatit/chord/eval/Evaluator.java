package com.lnatit.chord.eval;

import com.lnatit.chord.eval.intent.Intent;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.result.*;
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
        // TODO add debug tag user_override (u_*o), distinguish user(u) builtin(b) creator(c) player(p)
        return Optional.empty();
    }

    static ConflictCollector eval(KeySemantic subject, KeySemantic opponent) {
        ConflictCollector collector = new ConflictCollector();

        // State mutex
        evaluateStateMutex(subject, opponent, collector);
        if (collector.finished()) {
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

            evaluateResource((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);

            // Player intent (use T instead get I to avoid confusion with intercept)
            evaluateIntent((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);

            evaluateModality((KeySemantic.Advanced) subject, (KeySemantic.Advanced) opponent, collector);
        }

        return collector;
    }

    static void evaluateStateMutex(KeySemantic subject, KeySemantic opponent, ConflictCollector collector) {
        StateSet subjectStates = subject.states();
        StateSet opponentStates = opponent.states();
        if (StateSet.isMutex(subjectStates, opponentStates)) {
            // state_mutex
            collector.withDebug(ConflictTag.STATE_MUTEX);
            collector.setFinished();
            return;
        }

        boolean subjectIsSubset = subjectStates.isProperSubsetOf(opponentStates);
        if (subjectIsSubset || opponentStates.isProperSubsetOf(subjectStates)) {
            // state_subset
            collector.withRisk(new DynamicRisk.StateSubset(subjectIsSubset));
        }
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
            Optional<DynamicRisk.StateSubset> risk = collector.getRisk(DynamicRisk.StateSubset.class);
            if (risk.isPresent() && (risk.get().subjectIsSubset() == si || risk.get().subjectIsSubset() == !oi)) {
                risk.get().escalate();
                risk.get().setSeverity(si && oi ? Severity.WARNING : Severity.INFO);
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

    static void evaluateResource(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        Resource sRes = subjectSemantic.resource();
        Resource oRes = opponentSemantic.resource();
        if (Resource.overlaps(sRes, oRes)) {
            boolean isInterceptive = collector.getRisk(DynamicRisk.Interceptive.class).isPresent();
            if (!(subjectSemantic.readOnly() && opponentSemantic.readOnly())) {
                if (!subjectSemantic.readOnly() && !opponentSemantic.readOnly()) {
                    if (!sRes.supportsConcurrentWrites()) {
                        collector.withRisk(ConflictTag.CONCURRENT_WRITE,
                                           isInterceptive ? Severity.INFO : Severity.SEVERE);
                    }
                }
                else {
                    collector.withRisk(ConflictTag.READ_WRITE,
                                       sRes.supportsConcurrentWrites() || isInterceptive
                                       ? Severity.INFO
                                       : Severity.WARNING);
                }
            }
            else {
                collector.withDebug(ConflictTag.CONCURRENT_ACCESS);
            }
            return;
        }
        collector.withDebug(ConflictTag.RESOURCE_MUTEX);
    }

    static void evaluateIntent(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        List<Intent> sI = subjectSemantic.intents();
        List<Intent> oI = opponentSemantic.intents();
        if (Intent.hasShared(sI, oI)) {
            Optional<DynamicRisk.Interceptive> risk = collector.getRisk(DynamicRisk.Interceptive.class);
            if (risk.isPresent()) {
                risk.get().downgrade();
                return;
            }
            // intent_shared
            collector.withRisk(ConflictTag.INTENT_SHARE, Intent.isIdentical(sI, oI) ? Severity.SAFE : Severity.INFO);
        }
    }

    static void evaluateModality(
            KeySemantic.Advanced subjectSemantic,
            KeySemantic.Advanced opponentSemantic,
            ConflictCollector collector
    ) {
        Optional<DynamicRisk.ModalJudged> risk = collector.getRisk(DynamicRisk.ModalJudged.class);
        risk.ifPresent(modalJudged -> modalJudged.acceptModality(subjectSemantic.modality(), opponentSemantic.modality()));
        collector.withRisk(Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()));
    }
}

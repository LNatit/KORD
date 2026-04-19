package com.lnatit.chord.eval;

import com.lnatit.chord.eval.intent.IntentList;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.eval.override.OverrideManager;
import com.lnatit.chord.eval.resource.Resource;
import com.lnatit.chord.result.*;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.Map;
import java.util.Optional;

public interface Evaluator {
    /**
     * @see KeyMapping#same(KeyMapping)
     */
    static ConflictResult conflicts(KeyMapping subject, KeyMapping opponent) {
        ConflictCollector collector = new ConflictCollector();

        // Physical Pair
        if (!isSameKey(subject, opponent)) {
            // hardware_mismatch
            collector.withDebug(ConflictTag.HARDWARE_MISMATCH);
            return collector.toResult();
        }

        // User override
        Optional<ConflictResult> override = OverrideManager.getOverride(subject, opponent);
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

        evaluateResource(subject, opponent, collector);

        // Player intent (use T instead get I to avoid confusion with intercept)
        evaluateIntent(subject, opponent, collector);

        evaluateModality(subject, opponent, collector);

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

    static void evaluateResource(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictCollector collector
    ) {
        Resource sRes = subjectSemantic.resource();
        Resource oRes = opponentSemantic.resource();
        if (sRes == Resource.ROOT || oRes == Resource.ROOT) {
            collector.withDebug(ConflictTag.RESOURCE_MUTEX);
            return;
        }

        boolean subjectReadOnly = subjectSemantic.readOnly();
        boolean opponentReadOnly = opponentSemantic.readOnly();
        boolean interceptive = collector.getRisk(DynamicRisk.Interceptive.class).isPresent();
        if (sRes == oRes) {
            evaluateSameResource(subjectReadOnly, opponentReadOnly, interceptive, sRes, collector);
            return;
        }

        Resource lca = Resource.getLCA(sRes, oRes);
        if (lca == Resource.ROOT || (lca != sRes && lca != oRes)) {
            // Siblings or disjoint.
            collector.withDebug(ConflictTag.RESOURCE_MUTEX);
            return;
        }

        boolean subjectIsParent = lca == sRes;
        Resource parentResource = subjectIsParent ? sRes : oRes;
        Resource childResource = subjectIsParent ? oRes : sRes;
        evaluateAncestorResource(subjectReadOnly, opponentReadOnly,
                interceptive, parentResource, childResource, collector);
    }

    private static void evaluateSameResource(
            boolean subjectReadOnly,
            boolean opponentReadOnly,
            boolean interceptive,
            Resource resource,
            ConflictCollector collector
    ) {
        if (subjectReadOnly && opponentReadOnly) {
            collector.withDebug(ConflictTag.CONCURRENT_ACCESS);
            return;
        }

        if (!subjectReadOnly && !opponentReadOnly) {
            if (resource.allowsConcurrentWrites()) {
                collector.withDebug(ConflictTag.CONCURRENT_WRITE);
            } else {
                collector.withRisk(ConflictTag.CONCURRENT_MODIFICATION,
                        interceptive ? Severity.INFO : Severity.SEVERE);
            }
            return;
        }

        collector.withRisk(ConflictTag.READ_WRITE,
                resource.allowsConcurrentWrites() || interceptive
                        ? Severity.INFO
                        : Severity.WARNING);
    }

    private static void evaluateAncestorResource(
            boolean subjectReadOnly,
            boolean opponentReadOnly,
            boolean interceptive,
            Resource ancestor,
            Resource descendent,
            ConflictCollector collector
    ) {
        if (subjectReadOnly && opponentReadOnly) {
            collector.withDebug(ConflictTag.CONCURRENT_ACCESS);
            return;
        }

        boolean descendentAllowsConcurrentWrites = descendent.allowsConcurrentWrites();
        if (!subjectReadOnly && !opponentReadOnly) {
            boolean ancestorAllowsConcurrentWrites = ancestor.allowsConcurrentWrites();
            if (ancestorAllowsConcurrentWrites && descendentAllowsConcurrentWrites) {
                collector.withDebug(ConflictTag.CONCURRENT_WRITE);
                return;
            }

            collector.withRisk(ConflictTag.CONCURRENT_MODIFICATION,
                    interceptive ? Severity.INFO :
                            ancestorWWSeverity(ancestorAllowsConcurrentWrites,
                                    descendentAllowsConcurrentWrites));
            return;
        }

        collector.withRisk(ConflictTag.READ_WRITE,
                descendentAllowsConcurrentWrites || interceptive
                        ? Severity.INFO : Severity.WARNING);
    }

    private static Severity ancestorWWSeverity(
            boolean ancestorAllowsConcurrentWrites,
            boolean descendentAllowsConcurrentWrites
    ) {
        if (!ancestorAllowsConcurrentWrites && !descendentAllowsConcurrentWrites) {
            return Severity.SEVERE;
        }
        return ancestorAllowsConcurrentWrites ? Severity.WARNING : Severity.INFO;
    }

    static void evaluateIntent(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictCollector collector
    ) {
        IntentList sI = subjectSemantic.intents();
        IntentList oI = opponentSemantic.intents();
        if (IntentList.hasShared(sI, oI)) {
            Optional<DynamicRisk.Interceptive> risk = collector.getRisk(DynamicRisk.Interceptive.class);
            if (risk.isPresent()) {
                risk.get().downgrade();
                return;
            }
            // intent_shared
            collector.withRisk(ConflictTag.INTENT_SHARE, IntentList.isIdentical(sI, oI) ? Severity.SAFE : Severity.INFO);
        }
    }

    static void evaluateModality(
            KeySemantic subjectSemantic,
            KeySemantic opponentSemantic,
            ConflictCollector collector
    ) {
        Optional<DynamicRisk.ModalJudged> risk = collector.getRisk(DynamicRisk.ModalJudged.class);
        risk.ifPresent(modalJudged -> modalJudged.acceptModality(subjectSemantic.modality(), opponentSemantic.modality()));
        collector.withRisk(Modality.MATRIX.get(subjectSemantic.modality(), opponentSemantic.modality()));
    }
}

package com.lnatit.kord.eval;

import com.lnatit.kord.eval.intent.IntentList;
import com.lnatit.kord.eval.mutex.StateSet;
import com.lnatit.kord.result.*;
import com.lnatit.kord.result.risk.Collector;
import com.lnatit.kord.result.risk.Finalized;
import com.lnatit.kord.result.risk.RiskEntry;
import com.lnatit.kord.result.risk.Severity;
import com.lnatit.kord.result.risk.context.*;
import com.lnatit.kord.semantic.ContextSemantic;
import com.lnatit.kord.semantic.KeyContext;
import com.lnatit.kord.semantic.KeySemantic;
import com.lnatit.kord.semantic.SemanticalKey;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.util.List;

public interface Evaluator
{
    static Scene evalDynamic(KeyPair pair) {
        InputConstants.Key leftKey = pair.left().getKey();
        KeyModifier leftMod = pair.left().getKeyModifier();
        InputConstants.Key rightKey = pair.right().getKey();
        KeyModifier rightMod = pair.right().getKeyModifier();

        SameType key = SameType.DISJOINT;
        if (leftKey == rightKey) {
            key = SameType.MAIN;
        }
        else if (rightMod.matches(leftKey)) {
            key = SameType.MODIFIER;
        }
        SameType mod = SameType.DISJOINT;
        if (leftMod.matches(rightKey)) {
            mod = SameType.MODIFIER;
        }
        else if (leftMod == rightMod) {
            mod = SameType.MAIN;
        }

        return SameType.MATRIX.get(key, mod);
    }

    static Finalized evalStatic(KeyPair pair) {
        KeySemantic leftSemantic = ((SemanticalKey) pair.left()).kord$getSemantic();
        KeySemantic rightSemantic = ((SemanticalKey) pair.right()).kord$getSemantic();

        if (leftSemantic instanceof KeySemantic.Semantical(var leftMap)
            && rightSemantic instanceof KeySemantic.Semantical(var rightMap)) {
            List<KeyContext> overlapping = leftSemantic.getContexts().stream()
                    .filter(rightSemantic.getContexts()::contains)
                    .toList();

            if (overlapping.isEmpty()) {
                return Finalized.CONTEXT_MUTEX;
            }

            var collector = Collector.pipeline();
            for (var ctx : overlapping) {
                collector.add(ctx, evalContext(leftMap.get(ctx), rightMap.get(ctx)).collect());
            }
            return collector.collect();
        }

        var collector = Collector.context();
        for (var leftCtx : leftSemantic.getContexts()) {
            for (var rightCtx : rightSemantic.getContexts()) {
                if (isContextOverlapping(leftCtx, rightCtx)) {
                    collector.add(new KeyContext.Pair(leftCtx, rightCtx));
                }
            }
        }
        return collector.collect();
    }

    static boolean isContextOverlapping(KeyContext leftContext, KeyContext rightContext) {
        return leftContext.context().conflicts(rightContext.context()) || rightContext.context()
                                                                                      .conflicts(leftContext.context());
    }

    static ContextCollector evalContext(ContextSemantic left, ContextSemantic right) {
        ContextCollector collector = new ContextCollector();

        if (evaluateStateMutex(left, right, collector)) {
            return collector;
        }

        if (evaluateIntercept(left, right, collector)) {
            return collector;
        }

        if (evaluateRedirect(left, right, collector)) {
            return collector;
        }

        evaluateResource(left, right, collector);
        evaluateIntent(left, right, collector);
        evaluateModality(left, right, collector);
        return collector;
    }

    static boolean evaluateStateMutex(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        StateSet leftStates = left.states();
        StateSet rightStates = right.states();
        if (StateSet.isMutex(leftStates, rightStates)) {
            collector.setState(StateTag.STATE_MUTEX_RISK);
            return true;
        }

        boolean leftIsSubset = leftStates.isProperSubsetOf(rightStates);
        if (leftIsSubset || rightStates.isProperSubsetOf(leftStates)) {
            collector.setState(new StateTag.StateSubset(leftIsSubset));
            return false;
        }

        collector.setState(StateTag.STATE_INTERSECT_RISK);
        return false;
    }

    static boolean evaluateIntercept(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        boolean li = left.intercept();
        boolean ri = right.intercept();

        if (li || ri) {
            RiskEntry<StateTag> stateRisk = collector.state();
            if (stateRisk instanceof StateTag.StateSubset(boolean leftIsSubset) && (leftIsSubset == li || leftIsSubset == !ri)) {
                RiskEntry.Simple<InterceptTag> partialOverride = new RiskEntry.Simple<>(InterceptTag.PARTIAL_OVERRIDE);
                partialOverride.setSeverity(li && ri ? Severity.WARNING : Severity.INFO);
                collector.setIntercept(partialOverride);
                return true;
            }

            if (li && ri) {
                RiskEntry.Simple<InterceptTag> raceCondition = new RiskEntry.Simple<>(InterceptTag.RACE_CONDITION);
                raceCondition.setSeverity(Severity.SEVERE);
                collector.setIntercept(raceCondition);
                return false;
            }

            RiskEntry.Simple<InterceptTag> interceptInput = new RiskEntry.Simple<>(InterceptTag.INTERCEPT_INPUT);
            interceptInput.setSeverity(Severity.WARNING);
            collector.setIntercept(interceptInput);
            return false;
        }

        collector.setIntercept(RiskEntry.diagnostic(InterceptTag.CONCURRENT_INPUT));
        return false;
    }

    static boolean evaluateRedirect(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        RedirectMode.Info info = RedirectMode.MATRIX.get(left.redirectMode(), right.redirectMode());
        return info.attachTo(collector);
    }

    static void evaluateResource(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        Resource leftResource = left.resource();
        Resource rightResource = right.resource();
        if (leftResource == Resource.ROOT || rightResource == Resource.ROOT) {
            collector.setResource(RiskEntry.diagnostic(ResourceTag.RESOURCE_MUTEX));
            return;
        }

        boolean leftReadOnly = left.readOnly();
        boolean rightReadOnly = right.readOnly();
        RiskEntry<InterceptTag> intercept = collector.intercept();
        boolean interceptive = intercept != null && intercept.tag() != InterceptTag.CONCURRENT_INPUT;
        if (leftResource == rightResource) {
            evaluateSameResource(leftReadOnly, rightReadOnly, interceptive, leftResource, collector);
            return;
        }

        Resource lca = Resource.getLCA(leftResource, rightResource);
        if (lca == Resource.ROOT || (lca != leftResource && lca != rightResource)) {
            collector.setResource(RiskEntry.diagnostic(ResourceTag.RESOURCE_MUTEX));
            return;
        }

        boolean leftIsParent = lca == leftResource;
        Resource parentResource = leftIsParent ? leftResource : rightResource;
        Resource childResource = leftIsParent ? rightResource : leftResource;
        evaluateAncestorResource(leftReadOnly, rightReadOnly, interceptive, parentResource, childResource, collector);
    }

    private static void evaluateSameResource(
            boolean leftReadOnly,
            boolean rightReadOnly,
            boolean interceptive,
            Resource resource,
            ContextCollector collector
    ) {
        if (leftReadOnly && rightReadOnly) {
            collector.setResource(RiskEntry.diagnostic(ResourceTag.CONCURRENT_ACCESS));
            return;
        }

        if (!leftReadOnly && !rightReadOnly) {
            if (resource.allowsConcurrentWrites()) {
                collector.setResource(RiskEntry.diagnostic(ResourceTag.CONCURRENT_WRITE));
            }
            else {
                collector.setResource(RiskEntry.create(ResourceTag.CONCURRENT_WRITE,
                                                       interceptive ? Severity.INFO : Severity.SEVERE));
            }
            return;
        }

        collector.setResource(RiskEntry.create(ResourceTag.READ_WRITE,
                                               resource.allowsConcurrentWrites() || interceptive
                                               ? Severity.INFO
                                               : Severity.WARNING));
    }

    private static void evaluateAncestorResource(
            boolean leftReadOnly,
            boolean rightReadOnly,
            boolean interceptive,
            Resource ancestor,
            Resource descendant,
            ContextCollector collector
    ) {
        if (leftReadOnly && rightReadOnly) {
            collector.setResource(RiskEntry.diagnostic(ResourceTag.CONCURRENT_ACCESS));
            return;
        }

        boolean descendantAllowsConcurrentWrites = descendant.allowsConcurrentWrites();
        if (!leftReadOnly && !rightReadOnly) {
            boolean ancestorAllowsConcurrentWrites = ancestor.allowsConcurrentWrites();
            if (ancestorAllowsConcurrentWrites && descendantAllowsConcurrentWrites) {
                collector.setResource(RiskEntry.diagnostic(ResourceTag.CONCURRENT_WRITE));
                return;
            }

            collector.setResource(RiskEntry.create(ResourceTag.CONCURRENT_WRITE,
                                                   interceptive
                                                   ? Severity.INFO
                                                   : ancestorWWSeverity(ancestorAllowsConcurrentWrites,
                                                                        descendantAllowsConcurrentWrites)));
            return;
        }

        collector.setResource(RiskEntry.create(ResourceTag.READ_WRITE,
                                               descendantAllowsConcurrentWrites || interceptive
                                               ? Severity.INFO
                                               : Severity.WARNING));
    }

    private static Severity ancestorWWSeverity(
            boolean ancestorAllowsConcurrentWrites,
            boolean descendantAllowsConcurrentWrites
    ) {
        if (!ancestorAllowsConcurrentWrites && !descendantAllowsConcurrentWrites) {
            return Severity.SEVERE;
        }
        return ancestorAllowsConcurrentWrites ? Severity.WARNING : Severity.INFO;
    }

    static void evaluateIntent(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        IntentList leftIntent = left.intents();
        IntentList rightIntent = right.intents();
        if (IntentList.hasShared(leftIntent, rightIntent)) {
            RiskEntry<InterceptTag> intercept = collector.intercept();
            if (intercept instanceof RiskEntry.Simple<InterceptTag> mutable
                && intercept.tag() != InterceptTag.CONCURRENT_INPUT) {
                mutable.setSeverity(mutable.severity().downgrade());
            }
            collector.setIntent(RiskEntry.create(IntentTag.INTENT_SHARE,
                                                 IntentList.isIdentical(leftIntent, rightIntent)
                                                 ? Severity.SAFE
                                                 : Severity.INFO));
            return;
        }

        collector.setIntent(RiskEntry.diagnostic(IntentTag.INTENT_IRRELEVANT));
    }

    static void evaluateModality(ContextSemantic left, ContextSemantic right, ContextCollector collector) {
        RiskEntry<RedirectTag> redirect = collector.redirect();
        if (redirect instanceof RedirectTag.ModalDependent modalDependent) {
            modalDependent.acceptModality(left.modality(), right.modality());
        }
        collector.setModality(Modality.MATRIX.get(left.modality(), right.modality()));
    }
}

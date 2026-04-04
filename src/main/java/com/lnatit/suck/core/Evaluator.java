package com.lnatit.suck.core;

import com.lnatit.suck.core.result.*;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.List;
import java.util.Optional;

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
        Optional<ConflictResult> override = getUserOverride(subject, opponent);
        // TODO add debug tag user_override (u_*o), distinguish builtin(b) creator(c) user(u) player(p)
        if (override.isPresent()) {
            return override.get();
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

            evaluateModality((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);

            evaluateCategory((KeySemantic.Advanced) subjectSemantic, (KeySemantic.Advanced) opponentSemantic, collector);
        }

        collector.resolvePendingRisks();
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

    static Optional<ConflictResult> getUserOverride(KeyMapping subject, KeyMapping opponent) {
        // TODO to impl
        return Optional.empty();
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
        ConflictRisk.RaceCondition rc = collector.getRisk(ConflictRisk.RaceCondition.class);
        ConflictRisk.IntentShare is = collector.getRisk(ConflictRisk.IntentShare.class);
        if (rc != null && is != null) {
            // the only possible downgrade, although it's a WARNING...
            collector.withTag(rc.toTag());
            collector.withTag(is.toTag());

            // takes effect in escalation, so we remove it
            collector.remove(ConflictRisk.RaceCondition.class);
            collector.remove(ConflictRisk.IntentShare.class);

            // implement_contend
            collector.withTag(new ConflictTag.Simple("a_ic"), Severity.WARNING);
            return;
        }

        assert subjectSemantic.getClass() == opponentSemantic.getClass();
        ConflictTag.Pair pair;
        switch (subjectSemantic) {
            case KeySemantic.InGame ignored ->
                    pair = ActionCategory.InGame.MATRIX.get((ActionCategory.InGame) subjectSemantic.actionCategory(), (ActionCategory.InGame) opponentSemantic.actionCategory());
            case KeySemantic.InGui ignored ->
                    pair = ActionCategory.InGui.MATRIX.get((ActionCategory.InGui) subjectSemantic.actionCategory(), (ActionCategory.InGui) opponentSemantic.actionCategory());
        }
        collector.withPair(pair);
    }
}

package com.lnatit.kord.result.risk;

import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.semantic.KeyContext;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface RiskEntry<T extends RiskTag> extends ConflictRisk
{
    T tag();

    default Component display(KeyPair pair) {
        return Component.translatable(tag().translationKey());
    }

    static <T extends RiskTag> RiskEntry<T> create(T tag, Severity severity) {
        return new Immutable<>(tag, severity);
    }

    static <T extends RiskTag> RiskEntry<T> diagnostic(T tag) {
        return create(tag, Severity.SAFE);
    }

    record Immutable<T extends RiskTag>(T tag, Severity severity) implements RiskEntry<T>
    {
        @Override
        public Severity severity() {
            return this.severity;
        }
    }

    class Simple<T extends RiskTag> implements RiskEntry<T>
    {
        private final T tag;

        private Severity severity = Severity.SAFE;

        public Simple(T tag) {this.tag = tag;}

        @Override
        public T tag() {
            return this.tag;
        }

        @Override
        public Severity severity() {
            return this.severity;
        }

        public void setSeverity(Severity severity) {
            this.severity = severity;
        }
    }

    record ContextPairs(List<KeyContext.Pair> pairs) implements RiskEntry<RiskTag>
    {
        public ContextPairs {
            // TODO maybe no need an extra copy?
            pairs = List.copyOf(pairs);
        }

        @Override
        public RiskTag tag() {
            return RiskTag.CONTEXT_CONFLICT;
        }

        @Override
        public Severity severity() {
            return pairs().isEmpty() ? Severity.SAFE : Severity.SEVERE;
        }
    }
}

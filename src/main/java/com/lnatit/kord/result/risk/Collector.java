package com.lnatit.kord.result.risk;

import com.lnatit.kord.semantic.KeyContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Collector<R extends ConflictRisk> {
    R collect();

    static PipelineCollector pipeline() {
        return new PipelineCollector();
    }

    static ContextCollector context() {
        return new ContextCollector();
    }

    final class PipelineCollector implements Collector<Finalized> {
        private final Map<KeyContext, ConflictRisk.Packed> risks = new LinkedHashMap<>();

        public void add(KeyContext context, ConflictRisk.Packed risk) {
            this.risks.put(context, risk);
        }

        @Override
        public Finalized collect() {
            return new Finalized.Pipeline(this.risks);
        }
    }

    final class ContextCollector implements Collector<Finalized> {
        private final List<KeyContext.Pair> pairs = new ArrayList<>(2);

        public void add(KeyContext.Pair pair) {
            this.pairs.add(pair);
        }

        @Override
        public Finalized collect() {
            return this.pairs.isEmpty() ? Finalized.CONTEXT_MUTEX : Finalized.ofPairs(this.pairs);
        }
    }
}

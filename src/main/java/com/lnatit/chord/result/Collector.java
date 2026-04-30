package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Collector<R extends ConflictRisk> {
    R collect();

    static MappedCollector<KeyContext, ConflictRisk.Packed, Finalized.Pipeline> pipeline() {
        return new MappedCollector.Pipeline();
    }

    static MappedCollector<KeyContext.Pair, RiskEntry<?>, Finalized.Custom> custom() {
        return new MappedCollector.Custom();
    }

    abstract class MappedCollector<K, V extends ConflictRisk, R extends Finalized> implements Collector<R> {
        protected final Map<K, V> risks = new LinkedHashMap<>();

        public void add(K context, V risk) {
            this.risks.put(context, risk);
        }

        private static class Pipeline extends MappedCollector<KeyContext, ConflictRisk.Packed, Finalized.Pipeline> {
            @Override
            public Finalized.Pipeline collect() {
                return new Finalized.Pipeline(this.risks);
            }
        }

        private static class Custom extends MappedCollector<KeyContext.Pair, RiskEntry<?>, Finalized.Custom> {
            @Override
            public Finalized.Custom collect() {
                return this.risks.isEmpty() ? Finalized.Custom.EMPTY : new Finalized.Custom(this.risks);
            }
        }
    }
}

package com.lnatit.chord.result;

import com.lnatit.chord.semantic.KeyContext;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Collector<R extends ConflictRisk> {
    R collect();

    static MappedCollector<KeyContext, ConflictRisk.Packed, ConflictResult.Pipeline> pipeline() {
        return new MappedCollector.Pipeline();
    }

    static MappedCollector<KeyContext.Pair, RiskEntry, ConflictResult.Custom> custom() {
        return new MappedCollector.Custom();
    }

    abstract class MappedCollector<K, V extends ConflictRisk, R extends ConflictResult> implements Collector<R> {
        protected final Map<K, V> risks = new LinkedHashMap<>();

        public void add(K context, V risk) {
            this.risks.put(context, risk);
        }

        private static class Pipeline extends MappedCollector<KeyContext, ConflictRisk.Packed, ConflictResult.Pipeline> {
            @Override
            public ConflictResult.Pipeline collect() {
                return new ConflictResult.Pipeline(this.risks);
            }
        }

        private static class Custom extends MappedCollector<KeyContext.Pair, RiskEntry, ConflictResult.Custom> {
            @Override
            public ConflictResult.Custom collect() {
                return this.risks.isEmpty() ? ConflictResult.Custom.EMPTY : new ConflictResult.Custom(this.risks);
            }
        }
    }
}

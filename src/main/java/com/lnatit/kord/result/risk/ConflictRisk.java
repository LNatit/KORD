package com.lnatit.kord.result.risk;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConflictRisk
{
    Severity severity();

    default boolean isHidden() {
        return this.severity() == Severity.SAFE;
    }

    record Mapped<K, V extends ConflictRisk>(K key, V value) implements ConflictRisk
    {
        @Override
        public Severity severity() {
            return this.value.severity();
        }

        public static <K, V extends ConflictRisk> List<Mapped<K, V>> of(Map<K, V> map) {
            return map.entrySet().stream().map(e -> new Mapped<>(e.getKey(), e.getValue())).toList();
        }
    }

    record Packed(List<ConflictRisk> entries, Severity severity) implements ConflictRisk
    {
        public Packed(List<ConflictRisk> entries) {
            this(entries, resolveSeverity(entries));
        }

        @Override
        public Severity severity() {
            return this.severity;
        }
    }

    static Severity resolveSeverity(Collection<? extends ConflictRisk> risks) {
        Severity severity = Severity.SAFE;
        for (ConflictRisk risk : risks) {
            Severity s = risk.severity();
            if (s == Severity.SEVERE) {
                return s;
            }
            else if (s.ordinal() > severity.ordinal()) {
                severity = s;
            }
        }
        return severity;
    }
}

package com.lnatit.chord.eval.override;

import com.lnatit.chord.result.ConflictRisk;
import com.lnatit.chord.result.ConflictResult;
import com.lnatit.chord.result.Severity;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface OverrideManager
{
    List<OverrideType> PRIORITY = List.of(OverrideType.USER, OverrideType.PLAYER, OverrideType.CREATOR, OverrideType.BUILTIN);
    Map<OverrideType, Map<Pair, ConflictResult>> OVERRIDES = createOverrideMap();

    private static Map<OverrideType, Map<Pair, ConflictResult>> createOverrideMap() {
        Map<OverrideType, Map<Pair, ConflictResult>> map = new EnumMap<>(OverrideType.class);
        for (OverrideType type : OverrideType.values()) {
            map.put(type, new java.util.HashMap<>());
        }
        return map;
    }

    static void clear(OverrideType type) {
        OVERRIDES.get(type).clear();
    }

    static void clearAll() {
        for (OverrideType type : OverrideType.values()) {
            clear(type);
        }
    }

    static void put(OverrideType type, Pair pair, ConflictResult result) {
        OVERRIDES.get(type).put(pair, result);
    }

    static Optional<ConflictResult> getOverride(Pair pair) {
        for (OverrideType type : PRIORITY) {
            ConflictResult result = OVERRIDES.get(type).get(pair);
            if (result != null) {
                return Optional.of(withSourceTag(result, type));
            }
        }
        return Optional.empty();
    }

    static Optional<ConflictResult> getOverride(KeyMapping key1, KeyMapping key2) {
        return getOverride(Pair.of(key1, key2));
    }

    private static ConflictResult withSourceTag(ConflictResult result, OverrideType type) {
        List<ConflictRisk> risks = new ArrayList<>(result.risks());
        if (risks.stream().noneMatch(risk -> risk.tag().equals(type.tag()))) {
            risks.add(ConflictRisk.create(type.tag(), Severity.SAFE));
        }
        return new ConflictResult(result.severity(), risks);
    }

    record Pair(String key1, String key2)
    {
        public static Pair of(KeyMapping key1, KeyMapping key2) {
            return new Pair(key1.getName(), key2.getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Pair(String key3, String key4)) {
                return Objects.equals(key1(), key3) && Objects.equals(key2(), key4)
                       || Objects.equals(key1(), key4) && Objects.equals(key2(), key3);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key1()) ^ Objects.hashCode(key2());
        }
    }
}

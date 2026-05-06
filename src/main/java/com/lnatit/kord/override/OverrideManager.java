package com.lnatit.kord.override;

import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.result.ConflictResult;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OverrideManager
{
    List<OverrideType> PRIORITY = List.of(OverrideType.USER, OverrideType.PLAYER, OverrideType.CREATOR, OverrideType.BUILTIN);
    Map<OverrideType, Map<KeyPair, ConflictResult>> OVERRIDES = createOverrideMap();

    private static Map<OverrideType, Map<KeyPair, ConflictResult>> createOverrideMap() {
        Map<OverrideType, Map<KeyPair, ConflictResult>> map = new EnumMap<>(OverrideType.class);
        for (OverrideType type : OverrideType.values()) {
            map.put(type, new HashMap<>());
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

    static void put(OverrideType type, KeyPair pair, ConflictResult result) {
        OVERRIDES.get(type).put(pair, result);
    }

    @Nullable
    static ConflictResult getOverride(KeyPair pair) {
        for (OverrideType type : PRIORITY) {
            ConflictResult result = OVERRIDES.get(type).get(pair);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}

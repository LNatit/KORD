package com.lnatit.chord.gui;

import com.lnatit.chord.eval.Evaluator;
import com.lnatit.chord.eval.KeyPair;
import com.lnatit.chord.result.ConflictResult;
import com.lnatit.chord.result.risk.Finalized;
import com.lnatit.chord.result.risk.Severity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Backend
{
    public static final Map<KeyPair, Finalized> STATIC_CACHE = new HashMap<>();

    private static final List<ConflictResult> ACTIVE_RESULTS = new ArrayList<>();

    public static ConflictResult getResult(KeyMapping subject, KeyMapping opponent) {
        KeyPair pair = KeyPair.of(subject, opponent);
        if (!STATIC_CACHE.containsKey(pair)) {
            STATIC_CACHE.put(pair, Evaluator.evalStatic(pair));
        }
        return STATIC_CACHE.get(pair);
    }

    // 改绑时先清除MAP索引，然后修改绑定并写回索引，最后更新冲突列表
    public static void rebind(KeyMapping keyMapping, InputConstants.Key key, KeyModifier modifier) {
        ACTIVE_RESULTS.removeIf(byKeyMapping(keyMapping));
        keyMapping.setKeyModifierAndCode(modifier, key);
        for (KeyMapping entry : KeyMapping.MAP.getAll(key)) {
            if (entry == keyMapping) continue;
            ACTIVE_RESULTS.add(getResult(keyMapping, entry));
        }
    }

    @SafeVarargs
    public static List<ConflictResult> filter(Predicate<ConflictResult>... predicates) {
        return ACTIVE_RESULTS.stream().filter(result -> {
            for (Predicate<ConflictResult> predicate : predicates) {
                if (!predicate.test(result)) return false;
            }
            return true;
        }).toList();
    }

    public static Predicate<ConflictResult> byKey(InputConstants.Key key) {
        if (key == InputConstants.UNKNOWN) {
            return result -> false;
        }
        return result -> result.pair().left().getKey() == key || result.pair().right().getKey() == key;
    }

    public static Predicate<ConflictResult> byKeyMapping(KeyMapping keyMapping) {
        return result -> {
            KeyPair keyPair = result.pair();
            return keyPair.left() == keyMapping || keyPair.right() == keyMapping;
        };
    }

    public static Predicate<ConflictResult> bySeverity(Severity... severities) {
        return result -> {
            for (Severity severity : severities) {
                if (result.risk().severity() == severity) return true;
            }
            return false;
        };
    }
}

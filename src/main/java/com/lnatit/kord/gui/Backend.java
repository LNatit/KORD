package com.lnatit.kord.gui;

import com.lnatit.kord.eval.Evaluator;
import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.override.OverrideManager;
import com.lnatit.kord.result.ConflictResult;
import com.lnatit.kord.result.Scene;
import com.lnatit.kord.result.risk.Finalized;
import com.lnatit.kord.result.risk.Severity;
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
        ConflictResult override = OverrideManager.getOverride(pair);
        if (override != null) {
            return override;
        }
        Scene scene = getSceneOf(pair);
        Finalized risk = getFinalizedOf(pair);
        return new ConflictResult(pair, scene, risk);
    }

    public static Scene getSceneOf(KeyPair pair) {
        return Evaluator.evalDynamic(pair);
    }

    public static Finalized getFinalizedOf(KeyPair pair) {
        if (!STATIC_CACHE.containsKey(pair)) {
            STATIC_CACHE.put(pair, Evaluator.evalStatic(pair));
        }
        return STATIC_CACHE.get(pair);
    }

    // 改绑时先清除MAP索引，然后修改绑定并写回索引，最后更新冲突列表
    public static void rebind(KeyMapping keyMapping, InputConstants.Key key, KeyModifier modifier) {
        ACTIVE_RESULTS.removeIf(byKeyMapping(keyMapping));
        keyMapping.setKeyModifierAndCode(modifier, key);
        if (key.equals(InputConstants.UNKNOWN)) return;
        for (KeyMapping entry : KeyMapping.MAP.getAll(key)) {
            if (entry.equals(keyMapping)) continue;
            if (entry.isUnbound()) continue;
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
        if (key.equals(InputConstants.UNKNOWN)) {
            return result -> false;
        }
        return result -> result.pair().left().getKey().equals(key) || result.pair().right().getKey().equals(key);
    }

    public static Predicate<ConflictResult> byKeyMapping(final KeyMapping keyMapping) {
        return result -> {
            KeyPair keyPair = result.pair();
            return keyPair.left().equals(keyMapping) || keyPair.right().equals(keyMapping);
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

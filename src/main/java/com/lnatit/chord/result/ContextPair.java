package com.lnatit.chord.result;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.List;
import java.util.Objects;

public record ContextPair(String key1, String key2)
{
    public ContextPair {
        key1 = Objects.requireNonNull(key1, "key1");
        key2 = Objects.requireNonNull(key2, "key2");
        if (key1.compareTo(key2) > 0) {
            String tmp = key1;
            key1 = key2;
            key2 = tmp;
        }
    }

    public static ContextPair of(String key1, String key2) {
        return new ContextPair(key1, key2);
    }

    public static ContextPair of(IKeyConflictContext key1, IKeyConflictContext key2) {
        return of(contextKey(key1), contextKey(key2));
    }

    private static String contextKey(IKeyConflictContext context) {
        return context.getClass().getName() + "#" + context;
    }

    public record PairRiskEntry(ContextPair pair, List<ConflictRisk.Static> risks) {}
}


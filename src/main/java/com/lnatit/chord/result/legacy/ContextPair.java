package com.lnatit.chord.result.legacy;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.List;

public record ContextPair(String key1, String key2)
{
    // TODO: Introduce an IKeyConflictContext wrapper that carries translation key
    // and implements Comparable for deterministic ordered traversal.
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


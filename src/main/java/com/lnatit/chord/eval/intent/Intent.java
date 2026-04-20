package com.lnatit.chord.eval.intent;

import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record Intent(String name, int order) implements Comparable<Intent> {
    private static Map<String, Intent> cache = Map.of();
    private static boolean decoding = false;
    private static int nextOrder = 0;

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("all")
    public Intent {
    }

    @Override
    public int compareTo(Intent other) {
        return Integer.compare(this.order(), other.order());
    }

    public static synchronized Intent of(String name) {
        String cachedName = Objects.requireNonNull(name, "name");
        Intent existing = cache.get(cachedName);
        if (existing != null) {
            return existing;
        }
        if (!decoding) {
            throw new IllegalStateException("Unknown intent outside decode stage: " + cachedName);
        }
        Intent created = new Intent(cachedName, nextOrder);
        nextOrder++;
        cache.put(cachedName, created);
        return created;
    }

    public static synchronized void beginDecode() {
        decoding = true;
        cache = new LinkedHashMap<>();
    }

    public static synchronized void endDecode() {
        cache = Map.copyOf(cache);
        decoding = false;
        nextOrder = 0;
    }

    public static synchronized void clear() {
        cache = decoding ? new LinkedHashMap<>() : Map.of();
    }
}


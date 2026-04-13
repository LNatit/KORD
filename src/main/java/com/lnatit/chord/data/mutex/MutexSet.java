package com.lnatit.chord.data.mutex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MutexSet(String namespace, List<String> mutexes) {
    private static final Map<String, MutexSet> ALL_SETS = new HashMap<>();

    public static MutexSet get(String namespace) {
        return ALL_SETS.get(namespace);
    }

    public MutexSet(String namespace, List<String> mutexes) {
        if (mutexes.size() > 32)
            throw new IllegalArgumentException("A mutex set cannot contain more than 32 mutexes");
        this.namespace = namespace;
        this.mutexes = List.copyOf(mutexes);
        ALL_SETS.put(namespace, this);
    }

    public int getMask() {
        return (1 << mutexes.size()) - 1;
    }

    public int bitmapOf(List<String> mutexes) {
        // TODO
        return 0;
    }

    public List<String> mutexesOf(int bitmap) {
        // TODO
        return List.of();
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }
}

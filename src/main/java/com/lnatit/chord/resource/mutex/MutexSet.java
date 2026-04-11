package com.lnatit.chord.resource.mutex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutexSet
{
    private static final Map<String, MutexSet> ALL_SETS = new HashMap<>();

    private final String namespace;
    private final List<String> mutexes;

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

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }
}

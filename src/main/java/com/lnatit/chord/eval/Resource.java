package com.lnatit.chord.eval;

import java.util.HashMap;
import java.util.Map;

public record Resource(String id, boolean supportsConcurrentWrites) {
    private static final Map<String, Resource> RESOURCES = new HashMap<>();

    public static Resource create(String id, boolean supportsConcurrentWrites) {
        return RESOURCES.computeIfAbsent(id, k -> new Resource(k, supportsConcurrentWrites));
    }

    public static Resource create(String id) {
        return create(id, false);
    }

    public static Resource of(String id) {
        return RESOURCES.get(id);
    }

    public static boolean overlaps(Resource resource1, Resource resource2) {
        // TODO
        return false;
    }
}

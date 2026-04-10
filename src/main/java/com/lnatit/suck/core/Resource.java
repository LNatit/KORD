package com.lnatit.suck.core;

import java.util.HashMap;
import java.util.Map;

public class Resource {
    private static final Map<String, Resource> RESOURCES = new HashMap<>();

    public final String id;
    public final boolean supportsConcurrentWrites;

    private Resource(String id, boolean supportsConcurrentWrites) {
        this.id = id;
        this.supportsConcurrentWrites = supportsConcurrentWrites;
    }

    public static Resource of(String id, boolean supportsConcurrentWrites) {
        return RESOURCES.computeIfAbsent(id, k -> new Resource(k, supportsConcurrentWrites));
    }

    public static Resource of(String id) {
        return of(id, false);
    }

    public static boolean overlaps(Resource resource1, Resource resource2) {
        // TODO
        return false;
    }
}

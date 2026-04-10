package com.lnatit.suck.core;

import java.util.HashMap;
import java.util.Map;

public class Resource {
    private static Map<String, Resource> RESOURCES = new HashMap<>();


    private final String id;
    private final boolean supportsConcurrentWrites;

    public Resource(String id, boolean supportsConcurrentWrites) {
        this.id = id;
        this.supportsConcurrentWrites = supportsConcurrentWrites;
    }
}

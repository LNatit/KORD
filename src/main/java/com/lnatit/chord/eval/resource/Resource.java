package com.lnatit.chord.eval.resource;

import java.util.HashMap;
import java.util.Map;

public interface Resource
{
    Resource ROOT = new Resource() {
        @Override
        public String path() {
            return "";
        }

        @Override
        public boolean supportsConcurrentWrites() {
            return true;
        }
    };

    String path();

    boolean supportsConcurrentWrites();

    record Node(String path, boolean supportsConcurrentWrites) implements Resource
    {
    }

    Map<String, Resource> RESOURCES = new HashMap<>();

    static Resource create(String path, boolean supportsConcurrentWrites) {
        return RESOURCES.computeIfAbsent(path, p -> new Resource.Node(p, supportsConcurrentWrites));
    }

    static Resource create(String path) {
        return create(path, false);
    }

    static Resource of(String path) {
        return RESOURCES.get(path);
    }

    static boolean overlaps(Resource resource1, Resource resource2) {
        // TODO
        String[] parts1 = resource1.path().split("/");
        String[] parts2 = resource2.path().split("/");

        // 逐段比较，找到最后一个相同的前缀
        int minLen = Math.min(parts1.length, parts2.length);
        int lcaIndex = 0;
        while (lcaIndex < minLen && parts1[lcaIndex].equals(parts2[lcaIndex])) {
            lcaIndex++;
        }

        // 拼接 LCA 路径
        if (lcaIndex == 0) return false;

        return !of(String.join("/", java.util.Arrays.copyOf(parts1, lcaIndex))).supportsConcurrentWrites();
    }
}

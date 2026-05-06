package com.lnatit.kord.eval;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public record Resource(String path, boolean allowsConcurrentWrites) {
    private static final Map<String, Resource> RESOURCES = new HashMap<>();
    public static final Resource ROOT = new Resource("", true);

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("all")
    public Resource {
    }

    public static void clear() {
        RESOURCES.clear();
        RESOURCES.put("", ROOT);
    }

    public static Resource getOrCreate(String path) {
        String normalized = normalizePath(path);
        return RESOURCES.computeIfAbsent(normalized, p -> new Resource(p, false));
    }

    public static boolean define(String path, boolean supportsConcurrentWrites) {
        String normalized = normalizePath(path);
        boolean exists = RESOURCES.containsKey(normalized);
        RESOURCES.compute(normalized, (p, existing) -> {
            if (existing == null) {
                return new Resource(p, supportsConcurrentWrites);
            } else {
                boolean merged = existing.allowsConcurrentWrites() && supportsConcurrentWrites;
                return new Resource(p, merged);
            }
        });
        return exists;  // 返回 true 表示发生了合并（已存在），false 表示新建
    }

    public static Resource getLCA(Resource resource1, Resource resource2) {
        String[] parts1 = resource1.path().split("/");
        String[] parts2 = resource2.path().split("/");

        // 逐段比较，找到最后一个相同的前缀
        int minLen = Math.min(parts1.length, parts2.length);
        int lcaIndex = 0;
        while (lcaIndex < minLen && parts1[lcaIndex].equals(parts2[lcaIndex])) {
            lcaIndex++;
        }

        // 拼接 LCA 路径
        if (lcaIndex == 0) return ROOT;

        return getOrCreate(String.join("/", java.util.Arrays.copyOf(parts1, lcaIndex)));
    }


    private static String normalizePath(String path) {
        String normalized = path.strip().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}

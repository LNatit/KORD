package com.lnatit.chord.semantic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record KeyContext(String id, ConflictType type)
{
    private static final Map<String, KeyContext> ALL = new HashMap<>();
    public static final KeyContext IN_GAME = new KeyContext("in_game", ConflictType.SELF_ONLY);
    public static final KeyContext IN_GUI = new KeyContext("in_gui", ConflictType.SELF_ONLY);

    public static void init() {
        ALL.clear();
        register(IN_GAME);
        register(IN_GUI);
    }

    public static void register(KeyContext context) {
        ALL.put(context.id(), context);
    }

    @Nullable
    public static KeyContext get(String id) {
        return ALL.get(id);
    }

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("all")
    public KeyContext {}

    public enum ConflictType
    {
        NEVER,
        SELF_ONLY,
        CUSTOM
    }

    public CustomPair pair(KeyContext left, KeyContext right) {
        return new CustomPair(left, right);
    }

    public record CustomPair(KeyContext left, KeyContext right) {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public CustomPair {
            if (left.type() != ConflictType.CUSTOM || right.type() != ConflictType.CUSTOM) {
                throw new IllegalArgumentException("Only CUSTOM contexts can be paired");
            }
        }
    }
}

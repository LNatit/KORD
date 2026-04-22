package com.lnatit.chord.semantic.legacy;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record KeyContext(String id, ContextType type)
{
    private static final Map<String, KeyContext> ALL = new HashMap<>();
    public static final KeyContext IN_GAME = new KeyContext("in_game", ContextType.SELF_ONLY);
    public static final KeyContext IN_GUI = new KeyContext("in_gui", ContextType.SELF_ONLY);

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

    public CustomPair pair(KeyContext left, KeyContext right) {
        return new CustomPair(left, right);
    }

    public record CustomPair(KeyContext left, KeyContext right) {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public CustomPair {
            if (left.type() != ContextType.CUSTOM || right.type() != ContextType.CUSTOM) {
                throw new IllegalArgumentException("Only CUSTOM contexts can be paired");
            }
        }
    }
}

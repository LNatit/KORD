package com.lnatit.chord.semantic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record KeyContext(String id, ConflictType type)
{
    private static final Map<String, KeyContext> ALL = new HashMap<>();
    public static final KeyContext UNIVERSAL = new KeyContext("universal", ConflictType.ALWAYS);
    public static final KeyContext IN_GAME = new KeyContext("in_game", ConflictType.SELF_ONLY);
    public static final KeyContext IN_GUI = new KeyContext("in_gui", ConflictType.SELF_ONLY);

    public static void clear() {
        ALL.clear();
        ALL.put(UNIVERSAL.id, UNIVERSAL);
        ALL.put(IN_GAME.id, IN_GAME);
        ALL.put(IN_GUI.id, IN_GUI);
    }

    public static KeyContext register(String id, ConflictType type) {
        KeyContext context = new KeyContext(id, type);
        ALL.put(id, context);
        return context;
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
        ALWAYS
    }
}

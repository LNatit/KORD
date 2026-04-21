package com.lnatit.chord.semantic;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record KeyContext(String id, IKeyConflictContext context, ConflictType type)
{
    private static final Map<String, KeyContext> ALL = new HashMap<>();
    public static final KeyContext UNIVERSAL = new KeyContext("universal", KeyConflictContext.UNIVERSAL, ConflictType.ALWAYS);
    public static final KeyContext IN_GAME = new KeyContext("in_game", KeyConflictContext.IN_GAME, ConflictType.SELF_ONLY);
    public static final KeyContext IN_GUI = new KeyContext("in_gui", KeyConflictContext.GUI, ConflictType.SELF_ONLY);

    public static void init() {
        ALL.clear();
        register(UNIVERSAL);
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
        ALWAYS
    }
}

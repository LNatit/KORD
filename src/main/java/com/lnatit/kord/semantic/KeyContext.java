package com.lnatit.kord.semantic;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record KeyContext(String id, IKeyConflictContext context, ConflictType type)
{
    private static final Map<String, KeyContext> ALL = new HashMap<>();
    public static final KeyContext UNIVERSAL = new KeyContext("universal", KeyConflictContext.UNIVERSAL, ConflictType.CUSTOM);
    public static final KeyContext IN_GAME = new KeyContext("in_game", KeyConflictContext.IN_GAME, ConflictType.SELF_ONLY);
    public static final KeyContext IN_GUI = new KeyContext("in_gui", KeyConflictContext.GUI, ConflictType.SELF_ONLY);

    public static void init() {
        ALL.clear();
        ALL.put(UNIVERSAL.id, UNIVERSAL);
        ALL.put(IN_GAME.id, IN_GAME);
        ALL.put(IN_GUI.id, IN_GUI);
    }

    public static void register(KeyContext context) {
        ALL.put(context.id, context);
    }

    @Nullable
    public static KeyContext of(String id) {
        return ALL.get(id);
    }

    /**
     * Reverse-lookup: find a registered KeyContext whose wrapped IKeyConflictContext
     * is the same instance as {@code ctx}.  Returns {@code null} if none is registered.
     */
    @Nullable
    public static KeyContext lookup(IKeyConflictContext ctx) {
        for (KeyContext kc : ALL.values()) {
            if (kc.context() == ctx) return kc;
        }
        return null;
    }

    public static KeyContext anonymous(IKeyConflictContext ctx) {
        // TODO if it is an enum, then use ClassName+EnumName as id, else use ClassName
        return null;
    }

    public record Pair(KeyContext left, KeyContext right) {
        public Pair {
            if (left.type() != ConflictType.CUSTOM || right.type() != ConflictType.CUSTOM) {
                throw new IllegalArgumentException("All contexts in a pair must be of type CUSTOM");
            }
        }
    }
}

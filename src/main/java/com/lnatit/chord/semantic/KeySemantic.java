package com.lnatit.chord.semantic;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;

public sealed interface KeySemantic permits KeySemantic.Semantical, KeySemantic.RawContext
{
    SequencedCollection<KeyContext> getContexts();

    record Semantical(LinkedHashMap<KeyContext, ContextSemantic> semanticMap) implements KeySemantic
    {
        @ApiStatus.Internal
        public Semantical {}

        @Override
        public SequencedCollection<KeyContext> getContexts() {
            return semanticMap.sequencedKeySet();
        }
    }

    // Self only is allowed only in fallback, but not in datapack def
    record RawContext(KeyContext context) implements KeySemantic
    {
        @Override
        public SequencedCollection<KeyContext> getContexts() {
            return List.of(context);
        }
    }

    /**
     * Fallback for KeyMappings with no datapack-defined semantic.
     * Looks up the registered KeyContext for the given IKeyConflictContext instance;
     * if none is found, wraps it in an anonymous CUSTOM KeyContext so it routes
     * through CONTEXT_DIRECT without needing a separate Unknown branch.
     */
    static RawContext fallback(IKeyConflictContext ctx) {
        KeyContext known = KeyContext.lookup(ctx);
        if (known != null) return new RawContext(known);
        return new RawContext(new KeyContext("unknown:" + ctx.getClass().getName(), ctx, ConflictType.CUSTOM));
    }
}

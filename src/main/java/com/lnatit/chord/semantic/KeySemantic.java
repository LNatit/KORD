package com.lnatit.chord.semantic;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;

public sealed interface KeySemantic permits KeySemantic.Semantical, KeySemantic.RawContext
{
    SequencedCollection<KeyContext> getContexts();

    record Semantical(LinkedHashMap<KeyContext, ContextSemantic> semanticMap) implements KeySemantic
    {
        public Semantical {
            for (var entry : semanticMap.entrySet()) {
                if (entry.getKey().type() != ConflictType.SELF_ONLY) {
                    throw new IllegalArgumentException(
                            "Only SELF_ONLY contexts are allowed in Semantical key semantic, got: "
                            + entry.getKey().type());
                }
            }
        }

        @Override
        public SequencedCollection<KeyContext> getContexts() {
            return semanticMap.sequencedKeySet();
        }
    }

    record RawContext(KeyContext context) implements KeySemantic
    {
        public RawContext {
            if (context.type() == ConflictType.SELF_ONLY) {
                throw new IllegalArgumentException(
                        "SELF_ONLY contexts are not allowed in RawContext key semantic, got: "
                        + context.type());
            }
        }

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

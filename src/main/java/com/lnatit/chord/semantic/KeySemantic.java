package com.lnatit.chord.semantic;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public sealed interface KeySemantic permits KeySemantic.Semantical, KeySemantic.RawContext
{
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
    }
}

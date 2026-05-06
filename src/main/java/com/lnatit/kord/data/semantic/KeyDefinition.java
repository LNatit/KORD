package com.lnatit.kord.data.semantic;

import com.lnatit.kord.Kord;
import com.lnatit.kord.data.Requirement;
import com.lnatit.kord.semantic.ConflictType;
import com.lnatit.kord.semantic.ContextSemantic;
import com.lnatit.kord.semantic.KeyContext;
import com.lnatit.kord.semantic.KeySemantic;

import java.util.LinkedHashMap;
import java.util.List;

public record KeyDefinition(int version, Requirement requirement, String name, SemanticDefinition semantic)
{
    public boolean checkValid() {
        if (!requirement().isValid()) {
            Kord.LOGGER.debug(
                    "Key definition does not met the requirement: [modid = '{}', mod_version_range = '{}'], ignored.",
                    requirement().modid(),
                    requirement().mod_version_range());
            return false;
        }
        return true;
    }

    public sealed interface SemanticDefinition permits Semantical, RawContext {
        KeySemantic toSemantic();
    }

    public record Semantical(List<SemanticEntry> semantics) implements SemanticDefinition
    {
        @Override
        public KeySemantic toSemantic() {
            LinkedHashMap<KeyContext, ContextSemantic> map = new LinkedHashMap<>();
            for (var entry : semantics) {
                for (var context : entry.contexts()) {
                    if (map.containsKey(context)) {
                        Kord.LOGGER.warn(
                                "Duplicate KeyContext '{}', ignoring all but the first.",
                                context.id());
                        continue;
                    }
                    map.put(context, entry.semantic());
                }
            }
            return new KeySemantic.Semantical(map);
        }
    }

    public record RawContext(KeyContext context) implements SemanticDefinition
    {
        public RawContext {
            if (context.type() == ConflictType.SELF_ONLY) {
                throw new IllegalArgumentException(
                        "SELF_ONLY contexts are not allowed in RawContext, got: "
                        + context.type());
            }
        }

        @Override
        public KeySemantic toSemantic() {
            return new KeySemantic.RawContext(context);
        }
    }

    public record SemanticEntry(List<KeyContext> contexts, ContextSemantic semantic)
    {
        public SemanticEntry {
            for (var context : contexts) {
                if (context.type() != ConflictType.SELF_ONLY) {
                    throw new IllegalArgumentException(
                            "Only SELF_ONLY contexts are allowed in Semantic entry, got: "
                            + context.type());
                }
            }
        }
    }
}

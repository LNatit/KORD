package com.lnatit.chord.resource;

import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.context.IKeyContext;

import javax.annotation.Nullable;
import java.util.List;

public record KeyDefinitions(String version, String modid, @Nullable String mod_version_range, List<KeyDefinition> keys)
{
    public record KeyDefinition(String name, @Nullable String mod_version_range, List<SemanticEntry> semantics)
    {}

    public record SemanticEntry(List<IKeyContext> contexts, @Nullable String mod_version_range, KeySemantic semantic)
    {}
}

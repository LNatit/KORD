package com.lnatit.suck.core;

import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SemanticalKey extends IKeyMappingExtension
{
    void chord$addSemantic(KeyContext context, KeySemantic semantic);

    KeySemantic chord$getSemantic(KeyContext context);

    Set<Map.Entry<IKeyConflictContext, KeySemantic>> chord$getSemanticEntries();

    void chord$clearSemantics();

    default IKeyConflictContext chord$getSemanticalConflictCtx(KeyContext context) {
        return context.transform(getKeyConflictContext());
    }
}

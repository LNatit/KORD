package com.lnatit.suck.core;

import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public interface SemanticalKey extends IKeyMappingExtension
{
    void chord$addSemantic(KeyContext context, KeySemantic semantic);

    KeySemantic chord$getSemantic(KeyContext context);

    void chord$clearSemantics();

    default IKeyConflictContext chord$getSemanticalConflictCtx(KeyContext context) {
        return context.transform(getKeyConflictContext());
    }
}

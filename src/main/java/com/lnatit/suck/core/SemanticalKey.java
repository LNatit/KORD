package com.lnatit.suck.core;

import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public interface SemanticalKey extends IKeyMappingExtension
{
    void chord$setSemantic(KeySemantic semantic);

    KeySemantic chord$getSemantic();

    default IKeyConflictContext semanticalConflictCtx() {
        return chord$getSemantic().context().transform(getKeyConflictContext());
    }
}

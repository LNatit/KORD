package com.lnatit.suck.core;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public interface SemanticalKey
{
    IKeyConflictContext getKeyConflictContext();

    void setSemantic(KeySemantic semantic);

    KeySemantic getSemantic();

    default IKeyConflictContext semanticalConflictCtx() {
        return getSemantic().context().transform(getKeyConflictContext());
    }
}

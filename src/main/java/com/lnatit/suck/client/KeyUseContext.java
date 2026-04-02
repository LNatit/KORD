package com.lnatit.suck.client;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public sealed interface KeyUseContext extends IKeyConflictContext permits MenuContext, InGameContext
{
    KeyConflictContext getScene();


    @Override
    default boolean isActive() {
        return this.getScene().isActive();
    }

    @Override
    default boolean conflicts(IKeyConflictContext other) {
        return getScene().conflicts(other);
    }
}

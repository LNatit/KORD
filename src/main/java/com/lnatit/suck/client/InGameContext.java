package com.lnatit.suck.client;

import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class InGameContext implements KeyUseContext
{
    @Override
    public KeyConflictContext getScene() {
        return KeyConflictContext.IN_GAME;
    }
}

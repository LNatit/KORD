package com.lnatit.suck.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public sealed interface MenuContext extends KeyUseContext permits MenuContext.All, SpecificMenuContext
{
    static MenuContext all() {
        return All.INSTANCE;
    }

    static MenuContext of(ResourceLocation... menus) {

    }

    boolean activeOn(ResourceLocation menu);

    @Override
    default KeyConflictContext getScene() {
        return KeyConflictContext.GUI;
    }

    @Override
    default boolean conflicts(IKeyConflictContext other) {
        if (other instanceof KeyUseContext) {
            if (other instanceof MenuContext) {
                return true;
            }
            else if (other instanceof InGameContext) {
                return false;
            }
        }

        return KeyUseContext.super.conflicts(other);
    }

    final class All implements MenuContext
    {
        private static final All INSTANCE = new All();

        private All() {}

        @Override
        public boolean activeOn(ResourceLocation menu) {
            return true;
        }
    }
}

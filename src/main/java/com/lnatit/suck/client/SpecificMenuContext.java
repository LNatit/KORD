package com.lnatit.suck.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import java.util.Collections;
import java.util.List;

public final class SpecificMenuContext implements MenuContext
{
    public List<ResourceLocation> activeMenus() {

    }

    @Override
    public boolean activeOn(ResourceLocation menu) {
        return this.activeMenus().contains(menu);
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        if (other instanceof SpecificMenuContext context) {
            return !Collections.disjoint(this.activeMenus(), context.activeMenus());
        }

        return MenuContext.super.conflicts(other);
    }
}

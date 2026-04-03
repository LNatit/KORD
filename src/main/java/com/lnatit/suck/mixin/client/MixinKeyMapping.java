package com.lnatit.suck.mixin.client;

import com.lnatit.suck.core.KeySemantic;
import com.lnatit.suck.core.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey
{
    private KeySemantic semantic = KeySemantic.DEFAULT;

    @Shadow
    public abstract IKeyConflictContext getKeyConflictContext();

    @Override
    public void setSemantic(KeySemantic semantic) {
        this.semantic = semantic;
    }

    @Override
    public KeySemantic getSemantic() {
        return this.semantic;
    }
}

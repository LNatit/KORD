package com.lnatit.suck.mixin.client;

import com.lnatit.suck.core.KeySemantic;
import com.lnatit.suck.core.SemanticalKey;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey
{
    @Unique
    private KeySemantic chord$semantic = KeySemantic.DEFAULT;

    @Override
    public void chord$setSemantic(KeySemantic semantic) {
        this.chord$semantic = semantic;
    }

    @Override
    public KeySemantic chord$getSemantic() {
        return this.chord$semantic;
    }
}

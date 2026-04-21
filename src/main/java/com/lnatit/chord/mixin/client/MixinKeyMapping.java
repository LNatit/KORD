package com.lnatit.chord.mixin.client;

import com.lnatit.chord.semantic.KeySemantic;
import com.lnatit.chord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey
{
    @Unique
    private KeySemantic chord$semantic = KeySemantic.AS_IS;

    @Override
    public KeySemantic chord$getSemantic() {
        return this.chord$semantic;
    }

    @Override
    public void chord$setSemantic(KeySemantic semantic) {
        this.chord$semantic = semantic;
    }
}

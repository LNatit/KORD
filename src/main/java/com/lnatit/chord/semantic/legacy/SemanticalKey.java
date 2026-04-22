package com.lnatit.chord.semantic.legacy;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;

import javax.annotation.Nullable;

public interface SemanticalKey extends IKeyMappingExtension
{
    KeySemantic chord$getSemantic();

    void chord$setSemantic(KeySemantic semantic);

    @Nullable
    static KeyMapping lookup(String name) {
        return KeyMapping.ALL.get(name);
    }
}

package com.lnatit.chord.semantic;

import net.minecraft.client.KeyMapping;

import javax.annotation.Nullable;

public interface SemanticalKey
{
    KeySemantic chord$getSemantic();

    void chord$setSemantic(KeySemantic semantic);

    void chord$resetSemantic();

    int chord$compareTo(KeyMapping other);

    @Nullable
    static KeyMapping lookup(String name) {
        return KeyMapping.ALL.get(name);
    }
}

package com.lnatit.kord.semantic;

import net.minecraft.client.KeyMapping;

import javax.annotation.Nullable;

public interface SemanticalKey
{
    KeySemantic kord$getSemantic();

    boolean kord$isHoldModal();

    void kord$setSemantic(KeySemantic semantic);

    void kord$resetSemantic();

    int kord$compareTo(KeyMapping other);

    @Nullable
    static KeyMapping lookup(String name) {
        return KeyMapping.ALL.get(name);
    }
}

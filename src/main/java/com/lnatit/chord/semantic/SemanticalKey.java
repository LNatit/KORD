package com.lnatit.chord.semantic;

import com.lnatit.chord.eval.context.IKeyContext;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface SemanticalKey extends IKeyMappingExtension
{
    KeySemantic chord$getSemantic();

    void chord$setSemantic(KeySemantic semantic);

    @Nullable
    static KeyMapping lookup(String name) {
        return KeyMapping.ALL.get(name);
    }
}

package com.lnatit.suck.mixin.client;

import com.lnatit.suck.core.KeyContext;
import com.lnatit.suck.core.KeySemantic;
import com.lnatit.suck.core.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey {
    @Unique
    private final Map<IKeyConflictContext, KeySemantic> chord$semantics = new HashMap<>();


    @Override
    public void chord$addSemantic(KeyContext context, KeySemantic semantic) {
        this.chord$semantics.put(this.chord$getSemanticalConflictCtx(context), semantic);
    }

    @Override
    public KeySemantic chord$getSemantic(KeyContext context) {
        return this.chord$semantics.get(this.chord$getSemanticalConflictCtx(context));
    }

    @Override
    public void chord$clearSemantics() {
        this.chord$semantics.clear();
    }
}

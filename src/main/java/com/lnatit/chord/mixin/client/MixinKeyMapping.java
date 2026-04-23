package com.lnatit.chord.mixin.client;

import com.lnatit.chord.semantic.KeySemantic;
import com.lnatit.chord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey
{
    @Shadow private IKeyConflictContext keyConflictContext;
    @Shadow @Final private static Map<String, Integer> CATEGORY_SORT_ORDER;

    @Shadow public abstract String getCategory();

    @Shadow public abstract String getName();

    @Unique
    private final KeySemantic chord$fallback = KeySemantic.fallback(this.keyConflictContext);
    @Unique
    private KeySemantic chord$semantic = this.chord$fallback;

    @Override
    public KeySemantic chord$getSemantic() {
        return this.chord$semantic;
    }

    @Override
    public void chord$setSemantic(KeySemantic semantic) {
        this.chord$semantic = semantic;
    }

    @Override
    public void chord$resetSemantic() {
        this.chord$semantic = this.chord$fallback;
    }

    @Override
    public int chord$compareTo(KeyMapping other) {
        String tCat = this.getCategory();
        String oCat = other.getCategory();
        if (tCat.equals(oCat))
            return this.getName().compareTo(other.getName());
        Integer tOrder = CATEGORY_SORT_ORDER.get(tCat);
        Integer oOrder = CATEGORY_SORT_ORDER.get(oCat);
        if (tOrder == null && oOrder != null) return 1;
        if (tOrder != null && oOrder == null) return -1;
        if (tOrder == null && oOrder == null) return tCat.compareTo(oCat);
        return tOrder.compareTo(oOrder);
    }
}

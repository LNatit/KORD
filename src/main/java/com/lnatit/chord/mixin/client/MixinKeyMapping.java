package com.lnatit.chord.mixin.client;

import com.lnatit.chord.Chord;
import com.lnatit.chord.semantic.KeySemantic;
import com.lnatit.chord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements SemanticalKey
{
    @Shadow
    private IKeyConflictContext keyConflictContext;

    @Shadow
    public abstract KeyMapping.Category getCategory();

    @Shadow
    public abstract String getName();

    @Unique
    private final KeySemantic chord$fallback = KeySemantic.fallback(this.keyConflictContext);
    @Unique
    private KeySemantic chord$semantic = this.chord$fallback;

    @Redirect(method = {"<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;I)V", "<init>(Ljava/lang/String;Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;Lnet/neoforged/neoforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Key;Lnet/minecraft/client/KeyMapping$Category;)V"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <K, V> V chord$checkDuplication(Map<K, V> instance, K k, V v) {
        if (instance.containsKey(k)) {
            Chord.LOGGER.warn("Duplicate key registry detected! This may affect Chord system!");
        }
        return instance.put(k, v);
    }

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
        KeyMapping.Category tCat = this.getCategory();
        KeyMapping.Category oCat = other.getCategory();
        if (tCat.equals(oCat)) {return this.getName().compareTo(other.getName());}
        Integer tOrder = KeyMapping.Category.SORT_ORDER.get(tCat);
        Integer oOrder = KeyMapping.Category.SORT_ORDER.get(oCat);
        return tOrder.compareTo(oOrder);
    }
}

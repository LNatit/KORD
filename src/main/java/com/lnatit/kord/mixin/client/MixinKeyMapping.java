package com.lnatit.kord.mixin.client;

import com.lnatit.kord.Kord;
import com.lnatit.kord.eval.Modality;
import com.lnatit.kord.semantic.KeySemantic;
import com.lnatit.kord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.jetbrains.annotations.Nullable;
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
    private final KeySemantic kord$fallback = KeySemantic.fallback(this.keyConflictContext);
    @Unique
    private KeySemantic kord$semantic = this.kord$fallback;

    @Redirect(method = {"<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;I)V", "<init>(Ljava/lang/String;Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;Lnet/neoforged/neoforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Key;Lnet/minecraft/client/KeyMapping$Category;)V"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    @Nullable
    private static <K, V> V kord$checkDuplication(Map<K, V> instance, K k, V v) {
        if (instance.containsKey(k)) {
            Kord.LOGGER.warn("Duplicate key registry detected! This may affect Kord system!");
        }
        return instance.put(k, v);
    }

    @Override
    public KeySemantic kord$getSemantic() {
        return this.kord$semantic;
    }

    @Override
    public boolean kord$isHoldModal() {
        return switch (this.kord$semantic) {
            case KeySemantic.RawContext ignored -> false;
            case KeySemantic.Semantical semantical ->
                    semantical.semanticMap().values().stream().anyMatch(s -> s.modality() == Modality.HOLD);
        };
    }

    @Override
    public void kord$setSemantic(KeySemantic semantic) {
        this.kord$semantic = semantic;
    }

    @Override
    public void kord$resetSemantic() {
        this.kord$semantic = this.kord$fallback;
    }

    @Override
    public int kord$compareTo(KeyMapping other) {
        KeyMapping.Category tCat = this.getCategory();
        KeyMapping.Category oCat = other.getCategory();
        if (tCat.equals(oCat)) {return this.getName().compareTo(other.getName());}
        Integer tOrder = KeyMapping.Category.SORT_ORDER.indexOf(tCat);
        Integer oOrder = KeyMapping.Category.SORT_ORDER.indexOf(oCat);
        return tOrder.compareTo(oOrder);
    }
}

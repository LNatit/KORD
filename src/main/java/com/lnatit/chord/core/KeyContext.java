package com.lnatit.chord.core;

import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum KeyContext implements StringRepresentable
{
    AS_IS(c -> c == null ? KeyConflictContext.UNIVERSAL : c),
    UNIVERSAL(c -> KeyConflictContext.UNIVERSAL),
    IN_GAME(c -> KeyConflictContext.IN_GAME),
    IN_GUI(c -> KeyConflictContext.GUI);

    //Codecs

    private final String name = this.name().toLowerCase(Locale.ROOT);
    private final UnaryOperator<IKeyConflictContext> operator;

    KeyContext(UnaryOperator<IKeyConflictContext> operator) {this.operator = operator;}

    public IKeyConflictContext transform(IKeyConflictContext context) {
        return this.operator.apply(context);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}

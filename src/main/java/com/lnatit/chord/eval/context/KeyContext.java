package com.lnatit.chord.eval.context;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.util.function.UnaryOperator;

public enum KeyContext implements IKeyContext
{
    AS_IS(c -> c == null ? KeyConflictContext.UNIVERSAL : c),
    UNIVERSAL(c -> KeyConflictContext.UNIVERSAL),
    IN_GAME(c -> KeyConflictContext.IN_GAME),
    IN_GUI(c -> KeyConflictContext.GUI);

    private final UnaryOperator<IKeyConflictContext> operator;

    KeyContext(UnaryOperator<IKeyConflictContext> operator) {this.operator = operator;}

    @Override
    public IKeyConflictContext transform(IKeyConflictContext original) {
        return this.operator.apply(original);
    }
}

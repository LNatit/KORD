package com.lnatit.chord.eval.context;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public interface IKeyContext
{
    IKeyConflictContext transform(IKeyConflictContext original);

    record Lookup(String name) implements IKeyContext
    {
        @Override
        public IKeyConflictContext transform(IKeyConflictContext original) {
            // Look up all subclasses
            // TODO
            return KeyContext.valueOf(name).transform(original);
        }
    }
}

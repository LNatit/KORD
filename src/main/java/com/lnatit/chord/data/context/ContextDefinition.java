package com.lnatit.chord.data.context;

import com.lnatit.chord.semantic.KeyContext;

public record ContextDefinition(String id, String lookup, KeyContext.ConflictType type) {
    public KeyContext toKeyContext() {

    }
}

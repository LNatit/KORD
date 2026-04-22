package com.lnatit.chord.data.context;

import com.lnatit.chord.semantic.legacy.ContextType;
import com.lnatit.chord.semantic.legacy.KeyContext;

public record ContextDefinition(String id, String lookup, ContextType type) {
    public KeyContext toKeyContext() {

    }
}

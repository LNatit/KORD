package com.lnatit.chord.semantic.legacy;

import com.lnatit.chord.semantic.ContextSemantic;

import java.util.Map;

public interface KeySemantic
{
    KeySemantic AS_IS = new KeySemantic() {};

    record Precise(Map<KeyContext, ContextSemantic> semantics) implements KeySemantic
    {}
}

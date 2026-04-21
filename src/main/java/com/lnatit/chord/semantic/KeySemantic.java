package com.lnatit.chord.semantic;

import java.util.Map;

public interface KeySemantic
{
    KeySemantic AS_IS = new KeySemantic() {};

    record Precise(Map<KeyContext, ContextSemantic> semantics) implements KeySemantic
    {}
}

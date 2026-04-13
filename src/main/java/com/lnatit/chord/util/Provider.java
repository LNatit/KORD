package com.lnatit.chord.util;

@FunctionalInterface
public interface Provider<V>
{
    V get(boolean isReal);
}

package com.lnatit.chord.util;

@FunctionalInterface
public interface Supplier<V> extends Provider<V>
{
    V get();

    default V get(boolean isReal) {
        return get();
    }
}

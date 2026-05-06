package com.lnatit.kord.util;

@FunctionalInterface
public interface Supplier<V> extends Provider<V>
{
    V get();

    default V get(boolean isReal) {
        return get();
    }
}

package com.lnatit.kord.util;

@FunctionalInterface
public interface Provider<V>
{
    V get(boolean isReal);
}

package com.lnatit.chord.eval.mutex;

import com.lnatit.chord.resource.mutex.MutexSet;

public record Leaf(MutexSet mutexSet, int mask) implements Node
{
    @Override
    public StateSet toStateSet() {
        return null;
    }
}

package com.lnatit.chord.eval.mutex.tree;

import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.resource.mutex.MutexSet;

public record LeafNode(MutexSet mutexSet, int bitmap) implements TreeNode
{
    @Override
    public StateSet toStateSet() {
        return StateSet.HyperRect.singleton(mutexSet, bitmap);
    }
}

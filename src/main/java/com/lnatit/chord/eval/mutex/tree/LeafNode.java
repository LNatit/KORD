package com.lnatit.chord.eval.mutex.tree;

import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.data.mutex.MutexSet;

import java.util.List;

public record LeafNode(MutexSet mutexSet, int bitmap) implements TreeNode
{
    public static LeafNode of(String namespace, List<String> mutexes) {
        // TODO
    }


    @Override
    public StateSet toStateSet() {
        return StateSet.HyperRect.singleton(this.mutexSet(), this.bitmap());
    }

    public String namespace() {
        return this.mutexSet().namespace();
    }

    public List<String> mutexes() {
        return this.mutexSet().mutexesOf(this.bitmap());
    }
}

package com.lnatit.kord.eval.mutex.tree;

import com.lnatit.kord.eval.mutex.StateSet;
import com.lnatit.kord.data.mutex.MutexSet;

import java.util.List;

public record LeafNode(MutexSet mutexSet, int bitmap) implements TreeNode
{
    public static LeafNode of(String namespace, List<String> mutexes) {
        MutexSet mutex = MutexSet.get(namespace);
        return new LeafNode(mutex, mutex.bitmapOf(mutexes));
    }


    @Override
    public StateSet toStateSet() {
        return StateSet.singleton(this.mutexSet(), this.bitmap());
    }

    public String namespace() {
        return this.mutexSet().namespace();
    }

    public List<String> mutexes() {
        return this.mutexSet().mutexesOf(this.bitmap());
    }
}

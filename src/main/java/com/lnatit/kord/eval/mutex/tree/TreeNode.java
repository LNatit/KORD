package com.lnatit.kord.eval.mutex.tree;

import com.lnatit.kord.eval.mutex.StateSet;
import com.lnatit.kord.data.mutex.MutexSet;

import java.util.List;

public sealed interface TreeNode permits AndNode, LeafNode, NotNode, OrNode
{
    StateSet toStateSet();

    static TreeNode and(TreeNode... children) {
        return new AndNode(List.of(children));
    }

    static TreeNode or(TreeNode... children) {
        return new OrNode(List.of(children));
    }

    static TreeNode not(TreeNode child) {
        return new NotNode(child);
    }

    static TreeNode leaf(MutexSet mutexSet, int bitmap) {
        return new LeafNode(mutexSet, bitmap);
    }
}

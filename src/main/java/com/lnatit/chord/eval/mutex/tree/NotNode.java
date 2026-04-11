package com.lnatit.chord.eval.mutex.tree;

import com.lnatit.chord.eval.mutex.StateSet;

public record NotNode(TreeNode child) implements TreeNode {
    @Override
    public StateSet toStateSet() {
        return child.toStateSet().complement();
    }
}

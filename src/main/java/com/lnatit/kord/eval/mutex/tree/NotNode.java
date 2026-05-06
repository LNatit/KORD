package com.lnatit.kord.eval.mutex.tree;

import com.lnatit.kord.eval.mutex.StateSet;

public record NotNode(TreeNode child) implements TreeNode {
    @Override
    public StateSet toStateSet() {
        return child.toStateSet().complement();
    }
}

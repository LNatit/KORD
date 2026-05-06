package com.lnatit.kord.eval.mutex.tree;

import com.lnatit.kord.eval.mutex.StateSet;

import java.util.List;

public record OrNode(List<TreeNode> children) implements TreeNode
{
    @Override
    public StateSet toStateSet() {
        if (children.isEmpty()) {
            return StateSet.EMPTY;
        }
        StateSet result = StateSet.EMPTY;
        for (TreeNode child : children) {
            result = result.union(child.toStateSet());
        }
        return result;
    }
}

package com.lnatit.kord.eval.mutex.tree;

import com.lnatit.kord.eval.mutex.StateSet;

import java.util.List;

// Default List = AND
public record AndNode(List<TreeNode> children) implements TreeNode
{
    @Override
    public StateSet toStateSet() {
        if (children.isEmpty()) {
            return StateSet.FULL;
        }
        StateSet result = children.getFirst().toStateSet();
        for (int i = 1; i < children.size(); i++) {
            StateSet childSet = children.get(i).toStateSet();
            result = result.intersect(childSet);
        }
        return result;
    }
}

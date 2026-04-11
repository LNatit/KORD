package com.lnatit.chord.eval.mutex;

import java.util.List;

public record OrNode(List<Node> children) implements Node
{
    public static OrNode of(Node... children) {
        return new OrNode(List.of(children));
    }

    @Override
    public StateSet toStateSet() {
        return null;
    }
}

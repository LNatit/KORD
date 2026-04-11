package com.lnatit.chord.eval.mutex;

import java.util.List;

public record AndNode(List<Node> children) implements Node
{
    public static AndNode of(Node... children) {
        return new AndNode(List.of(children));
    }

    @Override
    public StateSet toStateSet() {
        return null;
    }
}

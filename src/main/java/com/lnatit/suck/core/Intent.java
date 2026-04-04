package com.lnatit.suck.core;

import java.util.List;

public interface Intent {
    static boolean hasShared(List<String> intents1, List<String> intents2) {
        return false;
    }

    static boolean contains(List<String> parent, List<String> subset) {
        return false;
    }

    static boolean isIdentical(List<String> intents1, List<String> intents2) {
        return false;
    }


    enum InGame implements Intent {

    }

    enum InGui implements Intent {

    }
}

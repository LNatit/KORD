package com.lnatit.chord.eval.intent;

import java.util.List;

// TODO
public interface Intent {

    String name();

    static Intent of(String name) {
        //TODO
        return () -> name;
    }













    static boolean hasShared(List<Intent> intents1, List<Intent> intents2) {
        return false;
    }

    static boolean contains(List<Intent> parent, List<Intent> subset) {
        return false;
    }

    static boolean isIdentical(List<Intent> intents1, List<Intent> intents2) {
        return false;
    }
}

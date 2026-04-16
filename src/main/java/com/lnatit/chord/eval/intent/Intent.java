package com.lnatit.chord.eval.intent;

import java.util.HashSet;
import java.util.List;

// TODO
public interface Intent {

    String name();

    static Intent of(String name) {
        //TODO
        return () -> name;
    }













    static boolean hasShared(List<Intent> intents1, List<Intent> intents2) {
        for (Intent intent1 : intents1) {
            for (Intent intent2 : intents2) {
                if (intent1 == intent2) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean contains(List<Intent> parent, List<Intent> subset) {
        return new HashSet<>(parent).containsAll(subset);
    }

    static boolean isIdentical(List<Intent> intents1, List<Intent> intents2) {
        return false;
    }
}

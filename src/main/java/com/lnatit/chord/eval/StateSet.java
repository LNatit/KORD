package com.lnatit.chord.eval;

public interface StateSet {
    StateSet EMPTY = null;

    default boolean isBounded() {
        // TODO Empty => false
        return false;
    }

    static boolean isMutex(StateSet set1, StateSet set2) {
        return false;
    }
}

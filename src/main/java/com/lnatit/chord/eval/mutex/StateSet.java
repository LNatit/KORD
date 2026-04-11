package com.lnatit.chord.eval.mutex;

import com.lnatit.chord.resource.mutex.MutexSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Map;

public class StateSet
{
    StateSet EMPTY = null;

    public boolean isSubsetOf(StateSet other) {

    }

    public boolean isIdenticalWith(StateSet other) {

    }







    public boolean isBounded() {
        // TODO Empty => false
        return false;
    }

    public static boolean isMutex(StateSet set1, StateSet set2) {
        return false;
    }

    public record HyperRect(Object2IntMap<MutexSet> mutexSetToBitmap) {

    }
}

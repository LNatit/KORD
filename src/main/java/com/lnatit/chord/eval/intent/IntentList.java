package com.lnatit.chord.eval.intent;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public record IntentList(List<Intent> values) {
    public static final IntentList EMPTY = new IntentList(List.of());

    @Deprecated
    @ApiStatus.Internal
    @SuppressWarnings("all")
    public IntentList {
            ArrayList<Intent> normalized = new ArrayList<>(new HashSet<>(values));
            normalized.sort(Intent::compareTo);
            values = List.copyOf(normalized);
    }

    public static IntentList of(List<Intent> intents) {
        if (intents.isEmpty()) {
            return EMPTY;
        }
        return new IntentList(intents);
    }

    public boolean contains(IntentList subset) {
        if (this.values.size() < subset.values.size()) {
            return false;
        }
        int i = 0;
        int j = 0;
        while (i < this.values.size() && j < subset.values.size()) {
            int cmp = this.values.get(i).compareTo(subset.values.get(j));
            if (cmp < 0) {
                i++;
                continue;
            }
            if (cmp > 0) {
                return false;
            }
            i++;
            j++;
        }
        return j == subset.values.size();
    }

    public static boolean hasShared(IntentList intents1, IntentList intents2) {
        if (intents1.values.isEmpty() || intents2.values.isEmpty()) {
            return false;
        }
        int i = 0;
        int j = 0;
        while (i < intents1.values.size() && j < intents2.values.size()) {
            int cmp = intents1.values.get(i).compareTo(intents2.values.get(j));
            if (cmp < 0) {
                i++;
                continue;
            }
            if (cmp > 0) {
                j++;
                continue;
            }
            return true;
        }
        return false;
    }

    public static boolean isIdentical(IntentList intents1, IntentList intents2) {
        return intents1.values.equals(intents2.values);
    }
}


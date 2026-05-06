package com.lnatit.kord.data.mutex;

import com.lnatit.kord.Kord;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MutexSet(String namespace, List<String> mutexes) {
    private static final Map<String, MutexSet> ALL_SETS = new HashMap<>();

    @Nullable
    public static MutexSet get(String namespace) {
        return ALL_SETS.get(namespace);
    }

    public static void clear() {
        ALL_SETS.clear();
    }

    public MutexSet(String namespace, List<String> mutexes) {
        if (mutexes.size() > 32)
            throw new IllegalArgumentException("A mutex set cannot contain more than 32 mutexes");
        this.namespace = namespace;
        this.mutexes = List.copyOf(mutexes);
        ALL_SETS.put(namespace, this);
    }

    public int getMask() {
        return (1 << mutexes.size()) - 1;
    }

    public int bitmapOf(List<String> mutexes) {
        // TODO
        int bitmap = 0;
        for (String mutex : mutexes) {
            int index = this.mutexes.indexOf(mutex);
            if (index == -1)
                Kord.LOGGER.warn("Mutex '{}' not found in mutex set '{}', ignored.", mutex, namespace);
            else
                bitmap |= 1 << index;
        }
        bitmap &= getMask();
        return bitmap;
    }

    public List<String> mutexesOf(int bitmap) {
        List<String> mutexes = new ArrayList<>();
        for (int index = 0; index < this.mutexes.size(); index++) {
            if ((1 << index & bitmap) !=  0)
                mutexes.add(this.mutexes.get(index));
        }
        return mutexes;
    }

    /**
     * Computes a hash contribution for this MutexSet dimension.
     * The bitmap is first complemented within the valid mask so that a full-coverage
     * bitmap (== getMask()) contributes 0 — consistent with the "omitted dimension ==
     * full coverage" convention of {@link com.lnatit.kord.eval.mutex.StateSet.HyperRect}.
     * Callers always pass the raw bitmap; the flip is an implementation detail here.
     */
    public int hashCodeOf(int bitmap) {
        int flipped = getMask() & ~bitmap;
        if (flipped == 0) return 0;

        int filled = 0;
        for (int i = 0; i < 32; i += mutexes.size()) {
            filled |= flipped << i;
        }

        return filled & hashCode();
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }
}

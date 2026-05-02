package com.lnatit.chord.eval;

import net.minecraft.client.KeyMapping;

import java.util.Objects;

/**
 * Canonical key-pair identity used by cross-module key-to-key mappings (for example override tables).
 * <p>
 * Construction is intentionally restricted to {@link KeyMapping}-based factories so callers cannot bypass
 * the canonical ordering rule.
 */
public final class KeyPair {
    private final String leftId;
    private final String rightId;

    private KeyPair(String leftId, String rightId) {
        this.leftId = leftId;
        this.rightId = rightId;
    }

    public static KeyPair of(KeyMapping key1, KeyMapping key2) {
        String id1 = key1.getName();
        String id2 = key2.getName();
        return id1.compareTo(id2) <= 0 ? new KeyPair(id1, id2) : new KeyPair(id2, id1);
    }


    public String leftId() {
        return leftId;
    }

    public String rightId() {
        return rightId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyPair pair)) return false;
        return Objects.equals(leftId, pair.leftId) && Objects.equals(rightId, pair.rightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftId, rightId);
    }

    @Override
    public String toString() {
        return "KeyPair[" + leftId + ", " + rightId + "]";
    }
}


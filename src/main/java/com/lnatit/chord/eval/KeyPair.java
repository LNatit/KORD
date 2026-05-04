package com.lnatit.chord.eval;

import com.lnatit.chord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;

import java.util.Objects;

/**
 * Canonical key-pair identity used by cross-module key-to-key mappings (for example override tables).
 * <p>
 * Construction is intentionally restricted to {@link KeyMapping}-based factories so callers cannot bypass
 * the canonical ordering rule.
 */
public final class KeyPair {
    private final KeyMapping left;
    private final KeyMapping right;

    private KeyPair(KeyMapping left, KeyMapping right) {
        this.left = left;
        this.right = right;
    }

    public static KeyPair of(KeyMapping key1, KeyMapping key2) {
        return ((SemanticalKey) key1).chord$compareTo(key2) <= 0 ? new KeyPair(key1, key2) : new KeyPair(key2, key1);
    }

    public static KeyPair of(String id1, String id2) {
        KeyMapping key1 = SemanticalKey.lookup(id1);
        KeyMapping key2 = SemanticalKey.lookup(id2);
        if (key1 == null || key2 == null) {
            throw new IllegalArgumentException("Invalid key mapping IDs: " + id1 + ", " + id2);
        }
        return of(key1, key2);
    }

    public KeyMapping left() {
        return left;
    }

    public KeyMapping right() {
        return right;
    }

    public String leftId() {
        return left.getName();
    }

    public String rightId() {
        return right.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyPair pair)) return false;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftId(), rightId());
    }

    @Override
    public String toString() {
        return "KeyPair[" + leftId() + ", " + rightId() + "]";
    }
}


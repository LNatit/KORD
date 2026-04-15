package com.lnatit.chord.eval;

import com.lnatit.chord.result.ConflictResult;
import net.minecraft.client.KeyMapping;

import java.util.Objects;
import java.util.Optional;

public interface OverrideManager
{


    static Optional<ConflictResult> getOverride(KeyMapping key1, KeyMapping key2) {
        Pair pair = Pair.of(key1, key2);

        // TODO add debug tag user_override (u_*o), distinguish user(u) builtin(b) creator(c) player(p)
        return Optional.empty();
    }

    record Pair(String key1, String key2)
    {
        public static Pair of(KeyMapping key1, KeyMapping key2) {
            return new Pair(key1.getName(), key2.getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Pair(String key3, String key4)) {
                return Objects.equals(key1(), key3) && Objects.equals(key2(), key4)
                       || Objects.equals(key1(), key4) && Objects.equals(key2(), key3);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key1()) ^ Objects.hashCode(key2());
        }
    }
}

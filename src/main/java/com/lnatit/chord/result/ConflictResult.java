package com.lnatit.chord.result;

import com.lnatit.chord.semantic.SemanticalKey;
import net.minecraft.client.KeyMapping;

// TODO use a wrapped KeyPair for Map key
public record ConflictResult(KeyMapping left, KeyMapping right, Finalized risk)
{
    public ConflictResult {
        if (((SemanticalKey) left).chord$compareTo(right) > 0) {
            // swap to ensure left <= right
            KeyMapping temp = left;
            left = right;
            right = temp;
        }
    }
}

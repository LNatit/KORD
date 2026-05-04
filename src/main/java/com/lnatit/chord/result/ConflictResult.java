package com.lnatit.chord.result;

import com.lnatit.chord.eval.KeyPair;
import com.lnatit.chord.result.risk.Finalized;
import net.minecraft.client.KeyMapping;

public record ConflictResult(KeyPair pair, DispatchBehaviour dispatch, Finalized risk)
{
    public ConflictResult(KeyMapping left, KeyMapping right, Finalized risk) {
        this(KeyPair.of(left, right), risk);
    }
}

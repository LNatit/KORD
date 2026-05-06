package com.lnatit.kord.result;

import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.result.risk.Finalized;
import net.minecraft.client.KeyMapping;

public record ConflictResult(KeyPair pair, Scene scene, Finalized risk)
{
    public ConflictResult(KeyMapping left, KeyMapping right, Finalized risk) {

    }
}

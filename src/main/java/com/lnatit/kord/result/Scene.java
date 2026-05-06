package com.lnatit.kord.result;

import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.semantic.SemanticalKey;

public record Scene(boolean pressMatches, boolean releaseMatches)
{
    public boolean activeOn(KeyPair pair) {
        return pressMatches()
               || releaseMatches()
                  && ((SemanticalKey) pair.left()).kord$isHoldModal()
                  && ((SemanticalKey) pair.right()).kord$isHoldModal();
    }
}

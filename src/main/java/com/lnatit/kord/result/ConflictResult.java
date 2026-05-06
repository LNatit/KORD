package com.lnatit.kord.result;

import com.lnatit.kord.eval.KeyPair;
import com.lnatit.kord.override.Origin;
import com.lnatit.kord.result.risk.Finalized;
import com.lnatit.kord.result.risk.Severity;
import net.minecraft.network.chat.MutableComponent;

public interface ConflictResult
{
    KeyPair pair();

    Severity severity();

    record Evaluated(KeyPair pair, Scene scene, boolean active, Finalized risk) implements ConflictResult
    {
        public Evaluated(KeyPair pair, Scene scene, Finalized risk) {
            this(pair,
                 scene,
                 scene.activeOn(pair),
                 risk);
        }

        @Override
        public Severity severity() {
            return active() ? risk().severity() : Severity.SAFE;
        }
    }

    record Overridden(KeyPair pair,
                      MutableComponent component,
                      Severity severity,
                      Origin origin) implements ConflictResult {}
}

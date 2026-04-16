package com.lnatit.chord.eval;

import com.lnatit.chord.eval.intent.Intent;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.eval.resource.Resource;

import java.util.List;

public record KeySemantic(StateSet states,
                          boolean intercept,
                          RedirectMode redirectMode,
                          Resource resource,
                          boolean readOnly,
                          List<Intent> intents,
                          Modality modality)
{
    public static KeySemantic DEFAULT = new KeySemantic(
            StateSet.FULL,
            false,
            RedirectMode.NONE,
            null,
            false,
            List.of(),
            Modality.PRESS
    );
}

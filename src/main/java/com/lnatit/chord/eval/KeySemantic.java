package com.lnatit.chord.eval;

import com.lnatit.chord.eval.intent.IntentList;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.eval.resource.Resource;

public record KeySemantic(StateSet states,
                          boolean intercept,
                          RedirectMode redirectMode,
                          Resource resource,
                          boolean readOnly,
                          IntentList intents,
                          Modality modality)
{
    public static KeySemantic DEFAULT = new KeySemantic(
            StateSet.FULL,
            false,
            RedirectMode.NONE,
            Resource.ROOT,
            false,
            IntentList.EMPTY,
            Modality.PRESS
    );
}

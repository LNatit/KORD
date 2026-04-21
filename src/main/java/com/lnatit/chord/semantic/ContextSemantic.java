package com.lnatit.chord.semantic;

import com.lnatit.chord.eval.Modality;
import com.lnatit.chord.eval.RedirectMode;
import com.lnatit.chord.eval.intent.IntentList;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.eval.resource.Resource;

public record ContextSemantic(StateSet states,
                              boolean intercept,
                              RedirectMode redirectMode,
                              Resource resource,
                              boolean readOnly,
                              IntentList intents,
                              Modality modality)
{
    public static ContextSemantic DEFAULT = new ContextSemantic(
            StateSet.FULL,
            false,
            RedirectMode.NONE,
            Resource.ROOT,
            false,
            IntentList.EMPTY,
            Modality.PRESS
    );
}

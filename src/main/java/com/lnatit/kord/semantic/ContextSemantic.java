package com.lnatit.kord.semantic;

import com.lnatit.kord.eval.Modality;
import com.lnatit.kord.eval.RedirectMode;
import com.lnatit.kord.eval.intent.IntentList;
import com.lnatit.kord.eval.mutex.StateSet;
import com.lnatit.kord.eval.Resource;

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

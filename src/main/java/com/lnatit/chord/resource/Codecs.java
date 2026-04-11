package com.lnatit.chord.resource;

import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.Modality;
import com.lnatit.chord.eval.RedirectMode;
import com.lnatit.chord.eval.Resource;
import com.lnatit.chord.eval.context.IKeyContext;
import com.lnatit.chord.eval.intent.IntentSet;
import com.lnatit.chord.eval.mutex.StateSet;
import com.mojang.serialization.Codec;

public interface Codecs
{
    Codec<StateSet> STATES_CODEC;
    Codec<RedirectMode> REDIRECT_CODEC;
    Codec<Resource> RESOURCE_CODEC;
    Codec<IntentSet> INTENTS_CODEC;
    Codec<Modality> MODALITY_CODEC;

    Codec<KeySemantic> SEMANTIC_CODEC;
    Codec<IKeyContext> CONTEXT_CODEC;

    Codec<KeyDefinitions.SemanticEntry> SEMANTIC_ENTRY_CODEC;

    Codec<KeyDefinitions.KeyDefinition> KEY_DEFINITION_CODEC;

    Codec<KeyDefinitions> KEYS_CODEC;






}

package com.lnatit.chord.resource;

import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.Modality;
import com.lnatit.chord.eval.RedirectMode;
import com.lnatit.chord.eval.Resource;
import com.lnatit.chord.eval.context.IKeyContext;
import com.lnatit.chord.eval.intent.IntentSet;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.resource.semantic.KeyDefinitions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface Codecs
{
    // TODO optimize listCodec

    Codec<StateSet> STATES_CODEC;
    Codec<RedirectMode> REDIRECT_CODEC;
    Codec<Resource> RESOURCE_CODEC;
    Codec<IntentSet> INTENTS_CODEC;
    Codec<Modality> MODALITY_CODEC;

    Codec<KeySemantic> SEMANTIC_CODEC;
    Codec<IKeyContext> CONTEXT_CODEC;

    Codec<KeyDefinitions.SemanticEntry> SEMANTIC_ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CONTEXT_CODEC.listOf().fieldOf("contexts").forGetter(KeyDefinitions.SemanticEntry::contexts),
            Codec.STRING.optionalFieldOf("mod_version_range")
                        .forGetter(KeyDefinitions.SemanticEntry::mod_version_range),
            SEMANTIC_CODEC.fieldOf("semantic").forGetter(KeyDefinitions.SemanticEntry::semantic)
    ).apply(inst, KeyDefinitions.SemanticEntry::new));

    Codec<KeyDefinitions.KeyDefinition> KEY_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(KeyDefinitions.KeyDefinition::name),
            Codec.STRING.optionalFieldOf("mod_version_range")
                        .forGetter(KeyDefinitions.KeyDefinition::mod_version_range),
            SEMANTIC_ENTRY_CODEC.listOf().fieldOf("semantics").forGetter(KeyDefinitions.KeyDefinition::semantics)
    ).apply(inst, KeyDefinitions.KeyDefinition::new));

    Codec<KeyDefinitions> KEYS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("version").forGetter(KeyDefinitions::version),
            Codec.STRING.fieldOf("modid").forGetter(KeyDefinitions::modid),
            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(KeyDefinitions::mod_version_range),
            KEY_DEFINITION_CODEC.listOf().fieldOf("keys").forGetter(KeyDefinitions::keys)
    ).apply(inst, KeyDefinitions::new));


}

package com.lnatit.chord.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lnatit.chord.data.mutex.MutexDefinition;
import com.lnatit.chord.data.mutex.MutexSet;
import com.lnatit.chord.eval.KeySemantic;
import com.lnatit.chord.eval.Modality;
import com.lnatit.chord.eval.RedirectMode;
import com.lnatit.chord.eval.context.IKeyContext;
import com.lnatit.chord.eval.context.KeyContext;
import com.lnatit.chord.eval.intent.Intent;
import com.lnatit.chord.eval.mutex.StateSet;
import com.lnatit.chord.data.semantic.KeyDefinitions;
import com.lnatit.chord.eval.mutex.tree.*;
import com.lnatit.chord.eval.resource.Resource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public interface Codecs {
    Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    Codec<Boolean> OPTIONAL_BOOL_CODEC = Codec.BOOL.orElse(false);

    Codec<MutexSet> MUTEX_SET_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("namespace").forGetter(MutexSet::namespace),
            Codec.STRING.listOf().fieldOf("mutexes").forGetter(MutexSet::mutexes)
    ).apply(inst, MutexSet::new));

    Codec<Requirement> REQUIREMENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("modid").forGetter(Requirement::modid),
            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(Requirement::mod_version_range)
    ).apply(inst, Requirement::new));

    Codec<MutexDefinition> MUTEX_DEFINITIONS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("namespace").forGetter(MutexDefinition::namespace),
            REQUIREMENT_CODEC.listOf().optionalFieldOf("requirements", List.of()).forGetter(MutexDefinition::requirements),
            Codec.STRING.listOf().fieldOf("mutexes").forGetter(MutexDefinition::mutexes)
    ).apply(inst, MutexDefinition::new));

    Codec<LeafNode> LEAF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("namespace").forGetter(leaf -> leaf.mutexSet().namespace()),
            Codec.STRING.listOf().fieldOf("mutexes").forGetter(leaf -> leaf.mutexSet().mutexes())
    ).apply(inst, LeafNode::of));
    Codec<AndNode> AND_CODEC = Codec.lazyInitialized(Codecs::andCodec);
    Codec<OrNode> OR_CODEC = Codec.lazyInitialized(Codecs::orCodec);
    Codec<NotNode> NOT_CODEC = Codec.lazyInitialized(Codecs::notCodec);
    Codec<TreeNode> TREE_CODEC =
            Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) LEAF_CODEC,
                    Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) AND_CODEC,
                            Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) OR_CODEC, NOT_CODEC)));

    // TODO optimize listCodec
    Codec<StateSet> STATES_CODEC = TREE_CODEC.xmap(TreeNode::toStateSet, stateSet -> new AndNode(List.of()));
    Codec<RedirectMode> REDIRECT_CODEC = enumCodec(RedirectMode.class).orElse(RedirectMode.NONE);
    Codec<Resource> RESOURCE_CODEC = Codec.STRING.xmap(Resource::of, Resource::path);
    Codec<Intent> INTENT_CODEC = Codec.STRING.xmap(Intent::of, Intent::name);
    Codec<Modality> MODALITY_CODEC = enumCodec(Modality.class).orElse(Modality.PRESS);

//    Codec<IKeyContext.Lookup> LOOKUP_CODEC;
    Codec<KeyContext> CONTEXT_CODEC = enumCodec(KeyContext.class).orElse(KeyContext.AS_IS);

    Codec<? extends IKeyContext> ICONTEXT_CODEC = CONTEXT_CODEC;
    Codec<KeySemantic> SEMANTIC_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            STATES_CODEC.fieldOf("states").forGetter(KeySemantic::states),
            OPTIONAL_BOOL_CODEC.fieldOf("intercept").forGetter(KeySemantic::intercept),
            REDIRECT_CODEC.fieldOf("redirect_mode").forGetter(KeySemantic::redirectMode),
            RESOURCE_CODEC.fieldOf("resource").forGetter(KeySemantic::resource),
            OPTIONAL_BOOL_CODEC.fieldOf("read_only").forGetter(KeySemantic::readOnly),
            INTENT_CODEC.listOf().fieldOf("intents").forGetter(KeySemantic::intents),
            MODALITY_CODEC.fieldOf("modality").forGetter(KeySemantic::modality)).apply(inst, KeySemantic::new));

    Codec<KeyDefinitions.SemanticEntry> SEMANTIC_ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ((Codec<IKeyContext>) ICONTEXT_CODEC).listOf().fieldOf("contexts").forGetter(KeyDefinitions.SemanticEntry::contexts),
            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(KeyDefinitions.SemanticEntry::mod_version_range),
            (SEMANTIC_CODEC).fieldOf("semantic").forGetter(KeyDefinitions.SemanticEntry::semantic)
    ).apply(inst, KeyDefinitions.SemanticEntry::new));

    Codec<KeyDefinitions.KeyDefinition> KEY_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("path").forGetter(KeyDefinitions.KeyDefinition::name),
            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(KeyDefinitions.KeyDefinition::mod_version_range),
            SEMANTIC_ENTRY_CODEC.listOf().fieldOf("semantics").forGetter(KeyDefinitions.KeyDefinition::semantics)
    ).apply(inst, KeyDefinitions.KeyDefinition::new));

    // 当前项目仍在开发中，没有兼容性需求。
    Codec<KeyDefinitions> KEYS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("version").forGetter(KeyDefinitions::version),
            REQUIREMENT_CODEC.fieldOf("requirement").forGetter(KeyDefinitions::requirement),
            KEY_DEFINITION_CODEC.listOf().fieldOf("keys").forGetter(KeyDefinitions::keys)
    ).apply(inst, KeyDefinitions::new));

    static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumType) {
        return Codec.STRING.xmap(str -> {
            try {
                return Enum.valueOf(enumType, str.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid value '" + str + "' for enum " + enumType.getSimpleName(), e);
            }
        }, Enum::name);
    }

    static Codec<AndNode> andCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                TREE_CODEC.listOf().fieldOf("children").forGetter(AndNode::children)
        ).apply(inst, AndNode::new));
    }

    static Codec<OrNode> orCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                TREE_CODEC.listOf().fieldOf("children").forGetter(OrNode::children)
        ).apply(inst, OrNode::new));
    }

    static Codec<NotNode> notCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                TREE_CODEC.fieldOf("child").forGetter(NotNode::child)
        ).apply(inst, NotNode::new));
    }
}

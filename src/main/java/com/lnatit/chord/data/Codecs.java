package com.lnatit.chord.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;

public interface Codecs
{
    Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    Codec<Boolean> OPTIONAL_BOOL_CODEC = Codec.BOOL.orElse(false);

//    Codec<Severity> SEVERITY_CODEC = enumCodec(Severity.class);
//
//    Codec<ConflictRisk.Static> CONFLICT_RISK_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.fieldOf("tag").forGetter(risk -> risk.tag().shortCode()),
//            OPTIONAL_BOOL_CODEC.optionalFieldOf("is_diagnostic", false).forGetter(risk -> risk.tag().isDiagnostic()),
//            SEVERITY_CODEC.fieldOf("severity").forGetter(ConflictRisk.Static::severity)
//    ).apply(inst, (tag, isDiagnostic, severity) ->
//            ConflictRisk.of(new ConflictTag(tag, isDiagnostic), severity)));
//
//    Codec<ContextPair> CONTEXT_PAIR_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.fieldOf("key1").forGetter(ContextPair::key1),
//            Codec.STRING.fieldOf("key2").forGetter(ContextPair::key2)
//    ).apply(inst, ContextPair::of));
//
//    Codec<ContextPair.PairRiskEntry> PAIR_RISK_ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            CONTEXT_PAIR_CODEC.fieldOf("context").forGetter(ContextPair.PairRiskEntry::pair),
//            CONFLICT_RISK_CODEC.listOf().optionalFieldOf("risks", List.of()).forGetter(ContextPair.PairRiskEntry::risks)
//    ).apply(inst, ContextPair.PairRiskEntry::new));
//
//    Codec<ConflictResult> CONFLICT_RESULT_CODEC = null;
//
//    Codec<Requirement> REQUIREMENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.fieldOf("modid").forGetter(Requirement::modid),
//            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(Requirement::mod_version_range)
//    ).apply(inst, Requirement::new));
//
//    Codec<OverrideDefinition.Key> OVERRIDE_KEY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            REQUIREMENT_CODEC.optionalFieldOf("requirement").forGetter(OverrideDefinition.Key::requirement),
//            Codec.STRING.fieldOf("name").forGetter(OverrideDefinition.Key::name)
//    ).apply(inst, OverrideDefinition.Key::new));
//
//    Codec<OverrideDefinition> OVERRIDE_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            OPTIONAL_BOOL_CODEC.fieldOf("is_builtin").forGetter(OverrideDefinition::isBuiltin),
//            OVERRIDE_KEY_CODEC.fieldOf("key1").forGetter(OverrideDefinition::key1),
//            OVERRIDE_KEY_CODEC.fieldOf("key2").forGetter(OverrideDefinition::key2),
//            CONFLICT_RESULT_CODEC.fieldOf("result").forGetter(OverrideDefinition::result)
//    ).apply(inst, OverrideDefinition::new));
//
//    Codec<MutexDefinition> MUTEX_DEFINITIONS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.optionalFieldOf("namespace").forGetter(MutexDefinition::namespace),
//            REQUIREMENT_CODEC.listOf()
//                             .optionalFieldOf("requirements", List.of())
//                             .forGetter(MutexDefinition::requirements),
//            Codec.STRING.listOf().fieldOf("mutexes").forGetter(MutexDefinition::mutexes)
//    ).apply(inst, MutexDefinition::new));
//
//    Codec<ResourceDefinition> RESOURCE_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            REQUIREMENT_CODEC.optionalFieldOf("requirement").forGetter(ResourceDefinition::requirement),
//            Codec.STRING.optionalFieldOf("path").forGetter(ResourceDefinition::path),
//            OPTIONAL_BOOL_CODEC.fieldOf("supports_concurrent_writes")
//                               .forGetter(ResourceDefinition::supportsConcurrentWrites)
//    ).apply(inst, ResourceDefinition::new));
//
//    Codec<LeafNode> LEAF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.fieldOf("namespace").forGetter(leaf -> leaf.mutexSet().namespace()),
//            Codec.STRING.listOf().fieldOf("mutexes").forGetter(leaf -> leaf.mutexSet().mutexes())
//    ).apply(inst, LeafNode::of));
//    Codec<AndNode> AND_CODEC = Codec.lazyInitialized(Codecs::andCodec);
//    Codec<OrNode> OR_CODEC = Codec.lazyInitialized(Codecs::orCodec);
//    Codec<NotNode> NOT_CODEC = Codec.lazyInitialized(Codecs::notCodec);
//    Codec<TreeNode> TREE_CODEC =
//            Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) LEAF_CODEC,
//                                  Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) AND_CODEC,
//                                                        Codec.withAlternative((Codec<TreeNode>) (Codec<? extends TreeNode>) OR_CODEC,
//                                                                              NOT_CODEC)));
//
//    // TODO optimize listCodec
//    Codec<StateSet> STATES_CODEC = TREE_CODEC.xmap(TreeNode::toStateSet, stateSet -> new AndNode(List.of()));
//    Codec<RedirectMode> REDIRECT_CODEC = enumCodec(RedirectMode.class).orElse(RedirectMode.NONE);
//    Codec<Resource> RESOURCE_CODEC = Codec.STRING.xmap(Resource::getOrCreate, Resource::path);
//    Codec<Intent> INTENT_CODEC = Codec.STRING.xmap(Intent::of, Intent::name);
//    Codec<IntentList> INTENT_LIST_CODEC = INTENT_CODEC.listOf().xmap(IntentList::of, IntentList::values);
//    Codec<Modality> MODALITY_CODEC = enumCodec(Modality.class).orElse(Modality.PRESS);
//
//    //    Codec<IKeyContext.Lookup> LOOKUP_CODEC;
//    Codec<KeyContext> CONTEXT_CODEC = enumCodec(KeyContext.class).orElse(KeyContext.AS_IS);
//
//    Codec<? extends IKeyContext> ICONTEXT_CODEC = CONTEXT_CODEC;
//    Codec<ContextSemantic> SEMANTIC_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            STATES_CODEC.fieldOf("states").forGetter(ContextSemantic::states),
//            OPTIONAL_BOOL_CODEC.fieldOf("intercept").forGetter(ContextSemantic::intercept),
//            REDIRECT_CODEC.fieldOf("redirect_mode").forGetter(ContextSemantic::redirectMode),
//            RESOURCE_CODEC.fieldOf("resource").forGetter(ContextSemantic::resource),
//            OPTIONAL_BOOL_CODEC.fieldOf("read_only").forGetter(ContextSemantic::readOnly),
//            INTENT_LIST_CODEC.fieldOf("intents").forGetter(ContextSemantic::intents),
//            MODALITY_CODEC.fieldOf("modality").forGetter(ContextSemantic::modality)).apply(inst, ContextSemantic::new));
//
//    Codec<KeyDefinition.SemanticEntry> SEMANTIC_ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            ((Codec<IKeyContext>) ICONTEXT_CODEC).listOf()
//                                                 .fieldOf("contexts")
//                                                 .forGetter(KeyDefinition.SemanticEntry::contexts),
//            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(KeyDefinition.SemanticEntry::modVersionRange),
//            (SEMANTIC_CODEC).fieldOf("semantic").forGetter(KeyDefinition.SemanticEntry::semantic)
//    ).apply(inst, KeyDefinition.SemanticEntry::new));
//
//    Codec<KeyDefinition.KeyDefinition> KEY_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.STRING.fieldOf("path").forGetter(KeyDefinition.KeyDefinition::name),
//            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(KeyDefinition.KeyDefinition::modVersionRange),
//            SEMANTIC_ENTRY_CODEC.listOf().fieldOf("semantics").forGetter(KeyDefinition.KeyDefinition::semantics)
//    ).apply(inst, KeyDefinition.KeyDefinition::new));
//
//    Codec<KeyDefinition> KEYS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.INT.fieldOf("version").forGetter(KeyDefinition::version),
//            REQUIREMENT_CODEC.fieldOf("requirement").forGetter(KeyDefinition::requirement),
//            KEY_DEFINITION_CODEC.listOf().fieldOf("keys").forGetter(KeyDefinition::keys)
//    ).apply(inst, KeyDefinition::new));
//
//    static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumType) {
//        return Codec.STRING.xmap(str -> {
//            try {
//                return Enum.valueOf(enumType, str.toUpperCase());
//            }
//            catch (IllegalArgumentException e) {
//                throw new RuntimeException("Invalid value '" + str + "' for enum " + enumType.getSimpleName(), e);
//            }
//        }, Enum::name);
//    }
//
//    static Codec<AndNode> andCodec() {
//        return RecordCodecBuilder.create(inst -> inst.group(
//                TREE_CODEC.listOf().fieldOf("children").forGetter(AndNode::children)
//        ).apply(inst, AndNode::new));
//    }
//
//    static Codec<OrNode> orCodec() {
//        return RecordCodecBuilder.create(inst -> inst.group(
//                TREE_CODEC.listOf().fieldOf("children").forGetter(OrNode::children)
//        ).apply(inst, OrNode::new));
//    }
//
//    static Codec<NotNode> notCodec() {
//        return RecordCodecBuilder.create(inst -> inst.group(
//                TREE_CODEC.fieldOf("child").forGetter(NotNode::child)
//        ).apply(inst, NotNode::new));
//    }
}

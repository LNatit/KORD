package com.lnatit.kord.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lnatit.kord.data.context.ContextDefinition;
import com.lnatit.kord.data.mutex.MutexDefinition;
import com.lnatit.kord.data.override.OverrideDefinition;
import com.lnatit.kord.data.resource.ResourceDefinition;
import com.lnatit.kord.data.semantic.KeyDefinition;
import com.lnatit.kord.eval.Modality;
import com.lnatit.kord.eval.RedirectMode;
import com.lnatit.kord.eval.intent.Intent;
import com.lnatit.kord.eval.intent.IntentList;
import com.lnatit.kord.eval.mutex.StateSet;
import com.lnatit.kord.eval.mutex.tree.AndNode;
import com.lnatit.kord.eval.mutex.tree.LeafNode;
import com.lnatit.kord.eval.mutex.tree.NotNode;
import com.lnatit.kord.eval.mutex.tree.OrNode;
import com.lnatit.kord.eval.mutex.tree.TreeNode;
import com.lnatit.kord.eval.Resource;
import com.lnatit.kord.result.risk.Severity;
import com.lnatit.kord.semantic.ConflictType;
import com.lnatit.kord.semantic.ContextSemantic;
import com.lnatit.kord.semantic.KeyContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public interface Codecs
{
    Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    Codec<Boolean> OPTIONAL_BOOL_CODEC = Codec.BOOL.orElse(false);

    Codec<Severity> SEVERITY_CODEC = enumCodec(Severity.class);

    // Use plain text component mapping for now; richer chat JSON codec can replace this later.
    Codec<MutableComponent> TEXT_COMPONENT_CODEC = Codec.STRING.xmap(Component::literal, Component::getString);

    Codec<Requirement> REQUIREMENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("modid").forGetter(Requirement::modid),
            Codec.STRING.optionalFieldOf("mod_version_range").forGetter(Requirement::mod_version_range)
    ).apply(inst, Requirement::new));

    Codec<MutexDefinition> MUTEX_DEFINITIONS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("namespace").forGetter(MutexDefinition::namespace),
            REQUIREMENT_CODEC.listOf()
                             .optionalFieldOf("requirements", List.of())
                             .forGetter(MutexDefinition::requirements),
            Codec.STRING.listOf().fieldOf("mutexes").forGetter(MutexDefinition::mutexes)
    ).apply(inst, MutexDefinition::new));

    Codec<ResourceDefinition> RESOURCE_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            REQUIREMENT_CODEC.optionalFieldOf("requirement").forGetter(ResourceDefinition::requirement),
            Codec.STRING.optionalFieldOf("path").forGetter(ResourceDefinition::path),
            OPTIONAL_BOOL_CODEC.fieldOf("supports_concurrent_writes")
                               .forGetter(ResourceDefinition::supportsConcurrentWrites)
    ).apply(inst, ResourceDefinition::new));

    Codec<ContextDefinition> CONTEXT_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            REQUIREMENT_CODEC.fieldOf("requirement").forGetter(ContextDefinition::requirement),
            Codec.STRING.fieldOf("id").forGetter(ContextDefinition::id),
            Codec.STRING.fieldOf("lookup").forGetter(ContextDefinition::lookup),
            enumCodec(ConflictType.class).fieldOf("type").forGetter(ContextDefinition::type)
    ).apply(inst, ContextDefinition::new));

    Codec<OverrideDefinition.Key> OVERRIDE_KEY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            REQUIREMENT_CODEC.optionalFieldOf("requirement").forGetter(OverrideDefinition.Key::requirement),
            Codec.STRING.fieldOf("name").forGetter(OverrideDefinition.Key::name)
    ).apply(inst, OverrideDefinition.Key::new));

    Codec<OverrideDefinition.Result> OVERRIDE_RESULT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TEXT_COMPONENT_CODEC.fieldOf("component").forGetter(OverrideDefinition.Result::component),
            SEVERITY_CODEC.fieldOf("severity").forGetter(OverrideDefinition.Result::severity)
    ).apply(inst, OverrideDefinition.Result::new));

    Codec<OverrideDefinition> OVERRIDE_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            OPTIONAL_BOOL_CODEC.fieldOf("is_builtin").forGetter(OverrideDefinition::isBuiltin),
            OVERRIDE_KEY_CODEC.fieldOf("key1").forGetter(OverrideDefinition::key1),
            OVERRIDE_KEY_CODEC.fieldOf("key2").forGetter(OverrideDefinition::key2),
            OVERRIDE_RESULT_CODEC.fieldOf("result").forGetter(OverrideDefinition::result)
    ).apply(inst, OverrideDefinition::new));

    Codec<LeafNode> LEAF_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("namespace").forGetter(LeafNode::namespace),
            Codec.STRING.listOf().fieldOf("mutexes").forGetter(LeafNode::mutexes)
    ).apply(inst, LeafNode::of));

    Codec<AndNode> AND_CODEC = Codec.lazyInitialized(Codecs::andCodec);
    Codec<OrNode> OR_CODEC = Codec.lazyInitialized(Codecs::orCodec);
    Codec<NotNode> NOT_CODEC = Codec.lazyInitialized(Codecs::notCodec);

    Codec<TreeNodeKind> TREE_NODE_KIND_CODEC = enumCodec(TreeNodeKind.class);

    MapCodec<LeafNode> LEAF_NODE_MAP_CODEC = LEAF_CODEC.fieldOf("data");
    MapCodec<AndNode> AND_NODE_MAP_CODEC = AND_CODEC.fieldOf("data");
    MapCodec<OrNode> OR_NODE_MAP_CODEC = OR_CODEC.fieldOf("data");
    MapCodec<NotNode> NOT_NODE_MAP_CODEC = NOT_CODEC.fieldOf("data");

    Codec<TreeNode> TREE_CODEC = TREE_NODE_KIND_CODEC.dispatch(
            "op",
            Codecs::treeNodeKind,
            kind -> switch (kind) {
                case LEAF -> LEAF_NODE_MAP_CODEC;
                case AND -> AND_NODE_MAP_CODEC;
                case OR -> OR_NODE_MAP_CODEC;
                case NOT -> NOT_NODE_MAP_CODEC;
            });

    // Encoding from StateSet back to tree is lossy today; decode path is the primary use.
    Codec<StateSet> STATES_CODEC = TREE_CODEC.xmap(TreeNode::toStateSet, stateSet -> new AndNode(List.of()));
    Codec<RedirectMode> REDIRECT_CODEC = enumCodec(RedirectMode.class).orElse(RedirectMode.NONE);
    Codec<Resource> RESOURCE_CODEC = Codec.STRING.xmap(Resource::getOrCreate, Resource::path);
    Codec<Intent> INTENT_CODEC = Codec.STRING.xmap(Intent::of, Intent::name);
    Codec<IntentList> INTENT_LIST_CODEC = INTENT_CODEC.listOf().xmap(IntentList::of, IntentList::values);
    Codec<Modality> MODALITY_CODEC = enumCodec(Modality.class).orElse(Modality.PRESS);

    Codec<KeyContext> CONTEXT_CODEC = Codec.STRING.comapFlatMap(Codecs::decodeKeyContext, KeyContext::id);

    Codec<ContextSemantic> SEMANTIC_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            STATES_CODEC.optionalFieldOf("states", StateSet.FULL).forGetter(ContextSemantic::states),
            OPTIONAL_BOOL_CODEC.optionalFieldOf("intercept", false).forGetter(ContextSemantic::intercept),
            REDIRECT_CODEC.optionalFieldOf("redirect_mode", RedirectMode.NONE).forGetter(ContextSemantic::redirectMode),
            RESOURCE_CODEC.optionalFieldOf("resource", Resource.ROOT).forGetter(ContextSemantic::resource),
            OPTIONAL_BOOL_CODEC.optionalFieldOf("read_only", false).forGetter(ContextSemantic::readOnly),
            INTENT_LIST_CODEC.optionalFieldOf("intents", IntentList.EMPTY).forGetter(ContextSemantic::intents),
            MODALITY_CODEC.optionalFieldOf("modality", Modality.PRESS).forGetter(ContextSemantic::modality)
    ).apply(inst, ContextSemantic::new));

    Codec<KeyDefinition.SemanticEntry> SEMANTIC_ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CONTEXT_CODEC.listOf().fieldOf("contexts").forGetter(KeyDefinition.SemanticEntry::contexts),
            SEMANTIC_CODEC.fieldOf("semantic").forGetter(KeyDefinition.SemanticEntry::semantic)
    ).apply(inst, KeyDefinition.SemanticEntry::new));

    Codec<KeyDefinition.Semantical> SEMANTICAL_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SEMANTIC_ENTRY_CODEC.listOf().fieldOf("semantics").forGetter(KeyDefinition.Semantical::semantics)
    ).apply(inst, KeyDefinition.Semantical::new));

    Codec<KeyDefinition.RawContext> RAW_CONTEXT_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CONTEXT_CODEC.fieldOf("context").forGetter(KeyDefinition.RawContext::context)
    ).apply(inst, KeyDefinition.RawContext::new));

    Codec<SemanticDefinitionKind> SEMANTIC_DEFINITION_KIND_CODEC = enumCodec(SemanticDefinitionKind.class);

    MapCodec<KeyDefinition.Semantical> SEMANTICAL_DEFINITION_MAP_CODEC = SEMANTICAL_DEFINITION_CODEC.fieldOf("data");
    MapCodec<KeyDefinition.RawContext> RAW_CONTEXT_DEFINITION_MAP_CODEC = RAW_CONTEXT_DEFINITION_CODEC.fieldOf("data");

    Codec<KeyDefinition.SemanticDefinition> SEMANTIC_DEFINITION_CODEC = SEMANTIC_DEFINITION_KIND_CODEC.dispatch(
            "kind",
            Codecs::semanticDefinitionKind,
            kind -> switch (kind) {
                case SEMANTICAL -> SEMANTICAL_DEFINITION_MAP_CODEC;
                case RAW_CONTEXT -> RAW_CONTEXT_DEFINITION_MAP_CODEC;
            });

    Codec<KeyDefinition> KEY_DEFINITION_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("version").forGetter(KeyDefinition::version),
            REQUIREMENT_CODEC.fieldOf("requirement").forGetter(KeyDefinition::requirement),
            Codec.STRING.fieldOf("name").forGetter(KeyDefinition::name),
            SEMANTIC_DEFINITION_CODEC.fieldOf("semantic").forGetter(KeyDefinition::semantic)
    ).apply(inst, KeyDefinition::new));
    static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumType) {
        return Codec.STRING.xmap(str -> {
            try {
                return Enum.valueOf(enumType, str.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid value '" + str + "' for enum " + enumType.getSimpleName(), e);
            }
        }, Enum::name);
    }

    private static DataResult<KeyContext> decodeKeyContext(String id) {
        KeyContext context = KeyContext.of(id);
        if (context == null) {
            return DataResult.error(() -> "Unknown KeyContext id: " + id);
        }
        return DataResult.success(context);
    }

    private static TreeNodeKind treeNodeKind(TreeNode node) {
        return switch (node) {
            case LeafNode ignored -> TreeNodeKind.LEAF;
            case AndNode ignored -> TreeNodeKind.AND;
            case OrNode ignored -> TreeNodeKind.OR;
            case NotNode ignored -> TreeNodeKind.NOT;
        };
    }

    private static SemanticDefinitionKind semanticDefinitionKind(KeyDefinition.SemanticDefinition definition) {
        return switch (definition) {
            case KeyDefinition.Semantical ignored -> SemanticDefinitionKind.SEMANTICAL;
            case KeyDefinition.RawContext ignored -> SemanticDefinitionKind.RAW_CONTEXT;
        };
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

    enum TreeNodeKind {
        LEAF,
        AND,
        OR,
        NOT
    }

    enum SemanticDefinitionKind {
        SEMANTICAL,
        RAW_CONTEXT
    }
}

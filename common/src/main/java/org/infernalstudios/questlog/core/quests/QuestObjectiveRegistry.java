package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockInteractObjective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockMineObjective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockPlaceObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.*;
import org.infernalstudios.questlog.core.quests.objectives.item.*;
import org.infernalstudios.questlog.core.quests.objectives.logic.AndObjective;
import org.infernalstudios.questlog.core.quests.objectives.logic.NotObjective;
import org.infernalstudios.questlog.core.quests.objectives.logic.OrObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.*;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class QuestObjectiveRegistry {

    private static final Map<ResourceLocation, Function<JsonObject, Objective>> REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, EditorMetadata> METADATA = new HashMap<>();

    static {
        // Block
        register(ResourceLocation.fromNamespaceAndPath("questlog", "block_mine"), BlockMineObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "block_place"), BlockPlaceObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "block_interact"), BlockInteractObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));

        // Entity
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_breed"), EntityBreedObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_death"), EntityDeathObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_kill"), EntityKillObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_approach"), EntityApproachObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_tame"), EntityTameObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));

        // Logic
        register(ResourceLocation.fromNamespaceAndPath("questlog", "and"), AndObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "or"), OrObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "not"), NotObjective::new,
                new EditorMetadata(null, null, "required_amount"));

        // Item
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_craft"), ItemCraftObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_drop"), ItemDropObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_equip"), ItemEquipObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_obtain"), ItemObtainObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_use"), ItemUseObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));

        // Misc
        register(ResourceLocation.fromNamespaceAndPath("questlog", "stat"), StatisticObjective::new,
                new EditorMetadata("stat", "Stat ID:", "required_amount", EditorMetadata.SuggestionType.CUSTOM_STAT));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "trample"), TrampleObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "enchant"), EnchantObjective::new,
                new EditorMetadata("enchantment", "Enchantment ID:", "required_amount", EditorMetadata.SuggestionType.ENCHANTMENT));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "effect_added"), EffectAddedObjective::new,
                new EditorMetadata("effect", "Effect ID:", "required_amount", EditorMetadata.SuggestionType.MOB_EFFECT));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_biome"), VisitBiomeObjective::new,
                new EditorMetadata("biome", "Biome ID:", "required_amount", EditorMetadata.SuggestionType.BIOME));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_dimension"), VisitDimensionObjective::new,
                new EditorMetadata("dimension", "Dimension:", "required_amount", EditorMetadata.SuggestionType.DIMENSION));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_position"), VisitPositionObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_structure"), VisitStructureObjective::new,
                new EditorMetadata("structure", "Structure ID:", "required_amount", EditorMetadata.SuggestionType.STRUCTURE));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "quest_complete"), QuestCompleteObjective::new,
                new EditorMetadata("quest", "Quest ID:", "required_amount", EditorMetadata.SuggestionType.QUEST));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "read"), ReadObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "advancement"), AdvancementObjective::new,
                new EditorMetadata("advancement", "Advancement ID:", "required_amount", EditorMetadata.SuggestionType.ADVANCEMENT));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "unobtainable"), UnobtainableObjective::new,
                new EditorMetadata(null, null, "required_amount"));
    }

    public static Set<ResourceLocation> getRegisteredTypes() {
        return REGISTRY.keySet();
    }

    public static void register(ResourceLocation id, Function<JsonObject, Objective> factory, EditorMetadata metadata) {
        REGISTRY.put(id, factory);
        METADATA.put(id, metadata);
    }

    public static EditorMetadata getMetadata(ResourceLocation id) {
        return METADATA.get(id);
    }

    public static Objective create(JsonObject definition) {
        String typeString = JsonUtils.getString(definition, "type");
        if (!typeString.contains(":")) {
            typeString = "questlog:" + typeString;
        }

        ResourceLocation type;
        try {
            type = ResourceLocation.parse(typeString);
        } catch (ResourceLocationException e) {
            throw new IllegalStateException("Invalid quest type: " + typeString);
        }

        return QuestObjectiveRegistry.create(type, definition);
    }

    public static Objective create(ResourceLocation type, JsonObject definition) {
        if (!REGISTRY.containsKey(type)) {
            throw new NullPointerException("Objective type not found: " + type);
        }
        try {
            return REGISTRY.get(type).apply(definition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objective of type " + type, e);
        }
    }
}
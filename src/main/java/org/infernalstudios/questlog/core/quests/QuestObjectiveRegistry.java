package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
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
        register(Questlog.id("questlog", "block_mine"), BlockMineObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(Questlog.id("questlog", "block_place"), BlockPlaceObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(Questlog.id("questlog", "block_interact"), BlockInteractObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));

        // Entity
        register(Questlog.id("questlog", "entity_breed"), EntityBreedObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(Questlog.id("questlog", "entity_death"), EntityDeathObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(Questlog.id("questlog", "entity_kill"), EntityKillObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(Questlog.id("questlog", "entity_approach"), EntityApproachObjective::new,
                new EditorMetadata("entity", "Entity ID:", "range", "Range (Blocks):", EditorMetadata.SuggestionType.ENTITY_TYPE));
        register(Questlog.id("questlog", "entity_tame"), EntityTameObjective::new,
                new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));

        // Logic
        register(Questlog.id("questlog", "and"), AndObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(Questlog.id("questlog", "or"), OrObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(Questlog.id("questlog", "not"), NotObjective::new,
                new EditorMetadata(null, null, "required_amount"));

        // Item
        register(Questlog.id("questlog", "item_craft"), ItemCraftObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(Questlog.id("questlog", "item_drop"), ItemDropObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(Questlog.id("questlog", "item_equip"), ItemEquipObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(Questlog.id("questlog", "item_obtain"), ItemObtainObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));
        register(Questlog.id("questlog", "item_use"), ItemUseObjective::new,
                new EditorMetadata("item", "Item ID:", "required_amount", EditorMetadata.SuggestionType.ITEM));

        // Misc
        register(Questlog.id("questlog", "stat"), StatisticObjective::new,
                new EditorMetadata("stat", "Stat ID:", "required_amount", EditorMetadata.SuggestionType.CUSTOM_STAT));
        register(Questlog.id("questlog", "trample"), TrampleObjective::new,
                new EditorMetadata("block", "Block ID:", "required_amount", EditorMetadata.SuggestionType.BLOCK));
        register(Questlog.id("questlog", "enchant"), EnchantObjective::new,
                new EditorMetadata("enchantment", "Enchantment ID:", "required_amount", EditorMetadata.SuggestionType.ENCHANTMENT));
        register(Questlog.id("questlog", "effect_added"), EffectAddedObjective::new,
                new EditorMetadata("effect", "Effect ID:", "required_amount", EditorMetadata.SuggestionType.MOB_EFFECT));
        register(Questlog.id("questlog", "visit_biome"), VisitBiomeObjective::new,
                new EditorMetadata("biome", "Biome ID:", "required_amount", EditorMetadata.SuggestionType.BIOME));
        register(Questlog.id("questlog", "visit_dimension"), VisitDimensionObjective::new,
                new EditorMetadata("dimension", "Dimension:", "required_amount", EditorMetadata.SuggestionType.DIMENSION));
        register(Questlog.id("questlog", "visit_position"), VisitPositionObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(Questlog.id("questlog", "visit_structure"), VisitStructureObjective::new,
                new EditorMetadata("structure", "Structure ID:", "required_amount", EditorMetadata.SuggestionType.STRUCTURE));
        register(Questlog.id("questlog", "quest_complete"), QuestCompleteObjective::new,
                new EditorMetadata("quest", "Quest ID:", "required_amount", EditorMetadata.SuggestionType.QUEST));
        register(Questlog.id("questlog", "read"), ReadObjective::new,
                new EditorMetadata(null, null, "required_amount"));
        register(Questlog.id("questlog", "advancement"), AdvancementObjective::new,
                new EditorMetadata("advancement", "Advancement ID:", "required_amount", EditorMetadata.SuggestionType.ADVANCEMENT));
        register(Questlog.id("questlog", "unobtainable"), UnobtainableObjective::new,
                new EditorMetadata(null, null, "required_amount"));

        // Origins
        register(Questlog.id("questlog", "origin"), OriginObjective::new,
                new EditorMetadata("origin", "Origin ID:", "required_amount", EditorMetadata.SuggestionType.ORIGIN));
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
            type = Questlog.parseId(typeString);
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
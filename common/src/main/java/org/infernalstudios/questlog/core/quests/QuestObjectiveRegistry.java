package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockMineObjective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockPlaceObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.*;
import org.infernalstudios.questlog.core.quests.objectives.item.*;
import org.infernalstudios.questlog.core.quests.objectives.logic.NotObjective;
import org.infernalstudios.questlog.core.quests.objectives.logic.OrObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.*;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QuestObjectiveRegistry {

    private static final Map<ResourceLocation, Function<JsonObject, Objective>> REGISTRY = new HashMap<>();

    static {
        // Block
        register(ResourceLocation.fromNamespaceAndPath("questlog", "block_mine"), BlockMineObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "block_place"), BlockPlaceObjective::new);

        // Entity
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_breed"), EntityBreedObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_death"), EntityDeathObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_kill"), EntityKillObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_approach"), EntityApproachObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "entity_tame"), EntityTameObjective::new);

        // Logic
        register(ResourceLocation.fromNamespaceAndPath("questlog", "or"), OrObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "not"), NotObjective::new);

        // Item
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_craft"), ItemCraftObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_drop"), ItemDropObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_equip"), ItemEquipObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_obtain"), ItemObtainObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_pickup"), ItemPickupObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item_use"), ItemUseObjective::new);

        // Misc
        register(ResourceLocation.fromNamespaceAndPath("questlog", "stat"), StatisticObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "trample"), TrampleObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "enchant"), EnchantObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "effect_added"), EffectAddedObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_biome"), VisitBiomeObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_dimension"), VisitDimensionObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_position"), VisitPositionObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "visit_structure"), VisitStructureObjective::new);
        register(ResourceLocation.fromNamespaceAndPath("questlog", "quest_complete"), QuestCompleteObjective::new);
    }

    public static void register(ResourceLocation id, Function<JsonObject, Objective> factory) {
        REGISTRY.put(id, factory);
    }

    public static Objective create(JsonObject definition) {
        ResourceLocation type;
        try {
            type = ResourceLocation.parse(JsonUtils.getString(definition, "type"));
        } catch (ResourceLocationException e) {
            throw new IllegalStateException("Invalid quest type: " + JsonUtils.getString(definition, "type"));
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

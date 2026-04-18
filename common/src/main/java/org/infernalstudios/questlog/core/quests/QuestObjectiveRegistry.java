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
        register(new ResourceLocation("questlog", "block_mine"), BlockMineObjective::new);
        register(new ResourceLocation("questlog", "block_place"), BlockPlaceObjective::new);
        register(new ResourceLocation("questlog", "block_interact"), BlockInteractObjective::new);

        // Entity
        register(new ResourceLocation("questlog", "entity_breed"), EntityBreedObjective::new);
        register(new ResourceLocation("questlog", "entity_death"), EntityDeathObjective::new);
        register(new ResourceLocation("questlog", "entity_kill"), EntityKillObjective::new);
        register(new ResourceLocation("questlog", "entity_approach"), EntityApproachObjective::new);
        register(new ResourceLocation("questlog", "entity_tame"), EntityTameObjective::new);

        // Logic
        register(new ResourceLocation("questlog", "or"), OrObjective::new);
        register(new ResourceLocation("questlog", "not"), NotObjective::new);

        // Item
        register(new ResourceLocation("questlog", "item_craft"), ItemCraftObjective::new);
        register(new ResourceLocation("questlog", "item_drop"), ItemDropObjective::new);
        register(new ResourceLocation("questlog", "item_equip"), ItemEquipObjective::new);
        register(new ResourceLocation("questlog", "item_obtain"), ItemObtainObjective::new);
        register(new ResourceLocation("questlog", "item_use"), ItemUseObjective::new);

        // Misc
        register(new ResourceLocation("questlog", "stat"), StatisticObjective::new);
        register(new ResourceLocation("questlog", "trample"), TrampleObjective::new);
        register(new ResourceLocation("questlog", "enchant"), EnchantObjective::new);
        register(new ResourceLocation("questlog", "effect_added"), EffectAddedObjective::new);
        register(new ResourceLocation("questlog", "visit_biome"), VisitBiomeObjective::new);
        register(new ResourceLocation("questlog", "visit_dimension"), VisitDimensionObjective::new);
        register(new ResourceLocation("questlog", "visit_position"), VisitPositionObjective::new);
        register(new ResourceLocation("questlog", "visit_structure"), VisitStructureObjective::new);
        register(new ResourceLocation("questlog", "quest_complete"), QuestCompleteObjective::new);
        register(new ResourceLocation("questlog", "read"), ReadObjective::new);
        register(new ResourceLocation("questlog", "advancement"), AdvancementObjective::new);
        register(new ResourceLocation("questlog", "unobtainable"), UnobtainableObjective::new);
    }

    public static void register(ResourceLocation id, Function<JsonObject, Objective> factory) {
        REGISTRY.put(id, factory);
    }

    public static Objective create(JsonObject definition) {
        String typeString = JsonUtils.getString(definition, "type");
        if (!typeString.contains(":")) {
            typeString = "questlog:" + typeString;
        }

        ResourceLocation type;
        try {
            type = new ResourceLocation(typeString);
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
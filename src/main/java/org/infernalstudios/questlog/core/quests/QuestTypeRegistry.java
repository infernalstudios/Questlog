package org.infernalstudios.questlog.core.quests;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.objectives.block.*;
import org.infernalstudios.questlog.core.quests.objectives.entity.*;
import org.infernalstudios.questlog.core.quests.objectives.item.*;
import org.infernalstudios.questlog.core.quests.objectives.misc.*;
import org.infernalstudios.questlog.util.PlayerReportableException;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public class QuestTypeRegistry {
  private static final Map<ResourceLocation, Function<JsonObject, Objective>> REGISTRY = new HashMap<>();

  static {
    // Block
    register(new ResourceLocation("questlog", "block_mine"), BlockMineObjective::new);
    register(new ResourceLocation("questlog", "block_place"), BlockPlaceObjective::new);

    // Entity
    register(new ResourceLocation("questlog", "entity_breed"), EntityBreedObjective::new);
    register(new ResourceLocation("questlog", "entity_death"), EntityDeathObjective::new);
    register(new ResourceLocation("questlog", "entity_kill"), EntityKillObjective::new);

    // Item
    register(new ResourceLocation("questlog", "item_craft"), ItemCraftObjective::new);
    register(new ResourceLocation("questlog", "item_drop"), ItemDropObjective::new);
    register(new ResourceLocation("questlog", "item_equip"), ItemEquipObjective::new);
    register(new ResourceLocation("questlog", "item_obtain"), ItemObtainObjective::new);
    register(new ResourceLocation("questlog", "item_pickup"), ItemPickupObjective::new);
    register(new ResourceLocation("questlog", "item_use"), ItemUseObjective::new);

    // Misc
    register(new ResourceLocation("questlog", "stat"), StatisticObjective::new);
    register(new ResourceLocation("questlog", "trample"), TrampleObjective::new);
    register(new ResourceLocation("questlog", "enchant"), EnchantObjective::new);
    register(new ResourceLocation("questlog", "effect_added"), EffectAddedObjective::new);
    register(new ResourceLocation("questlog", "visit_biome"), VisitBiomeObjective::new);
    register(new ResourceLocation("questlog", "visit_dimension"), VisitDimensionObjective::new);
    register(new ResourceLocation("questlog", "visit_position"), VisitPositionObjective::new);
  }

  public static void register(ResourceLocation id, Function<JsonObject, Objective> factory) {
    REGISTRY.put(id, factory);
  }

  public static Objective create(JsonObject definition) {
    ResourceLocation type;
    try {
      type = new ResourceLocation(definition.get("type").getAsString());
    } catch (ResourceLocationException e) {
      throw new IllegalStateException("Invalid quest type: " + definition.get("type").getAsString());
    }

    return QuestTypeRegistry.create(type, definition);
  }

  public static Objective create(ResourceLocation type, JsonObject definition) {
    if (!REGISTRY.containsKey(type)) {
      throw new PlayerReportableException("Quest type not found: " + type);
    }
    return REGISTRY.get(type).apply(definition);
  }
}

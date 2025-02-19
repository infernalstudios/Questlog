package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockMineObjective;
import org.infernalstudios.questlog.core.quests.objectives.block.BlockPlaceObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.EntityApproachObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.EntityBreedObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.EntityDeathObjective;
import org.infernalstudios.questlog.core.quests.objectives.entity.EntityKillObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemCraftObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemDropObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemEquipObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemObtainObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemPickupObjective;
import org.infernalstudios.questlog.core.quests.objectives.item.ItemUseObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.EffectAddedObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.EnchantObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.QuestCompleteObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.StatisticObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.TrampleObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.VisitBiomeObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.VisitDimensionObjective;
import org.infernalstudios.questlog.core.quests.objectives.misc.VisitPositionObjective;
import org.infernalstudios.questlog.util.JsonUtils;

public class QuestObjectiveRegistry {

  private static final Map<ResourceLocation, Function<JsonObject, Objective>> REGISTRY = new HashMap<>();

  static {
    // Block
    register(new ResourceLocation("questlog", "block_mine"), BlockMineObjective::new);
    register(new ResourceLocation("questlog", "block_place"), BlockPlaceObjective::new);

    // Entity
    register(new ResourceLocation("questlog", "entity_breed"), EntityBreedObjective::new);
    register(new ResourceLocation("questlog", "entity_death"), EntityDeathObjective::new);
    register(new ResourceLocation("questlog", "entity_kill"), EntityKillObjective::new);
    register(new ResourceLocation("questlog", "entity_approach"), EntityApproachObjective::new);

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
    register(new ResourceLocation("questlog", "quest_complete"), QuestCompleteObjective::new);
  }

  public static void register(ResourceLocation id, Function<JsonObject, Objective> factory) {
    REGISTRY.put(id, factory);
  }

  public static Objective create(JsonObject definition) {
    ResourceLocation type;
    try {
      type = new ResourceLocation(JsonUtils.getString(definition, "type"));
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

package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.rewards.CommandReward;
import org.infernalstudios.questlog.core.quests.rewards.ExperienceReward;
import org.infernalstudios.questlog.core.quests.rewards.ItemReward;
import org.infernalstudios.questlog.core.quests.rewards.LootTableReward;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.JsonUtils;

public class QuestRewardRegistry {

  private static final Map<ResourceLocation, Function<JsonObject, Reward>> REGISTRY = new HashMap<>();

  static {
    register(new ResourceLocation("questlog", "item"), ItemReward::new);
    register(new ResourceLocation("questlog", "command"), CommandReward::new);
    register(new ResourceLocation("questlog", "experience"), ExperienceReward::new);
    register(new ResourceLocation("questlog", "loot_table"), LootTableReward::new);
  }

  public static void register(ResourceLocation id, Function<JsonObject, Reward> factory) {
    REGISTRY.put(id, factory);
  }

  public static Reward create(JsonObject definition) {
    ResourceLocation type;
    try {
      type = new ResourceLocation(JsonUtils.getString(definition, "type"));
    } catch (ResourceLocationException e) {
      throw new IllegalStateException("Invalid reward type: " + JsonUtils.getString(definition, "type"));
    }

    return QuestRewardRegistry.create(type, definition);
  }

  public static Reward create(ResourceLocation type, JsonObject definition) {
    if (!REGISTRY.containsKey(type)) {
      throw new NullPointerException("Reward type not found: " + type);
    }

    try {
      return REGISTRY.get(type).apply(definition);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create reward of type " + type, e);
    }
  }
}

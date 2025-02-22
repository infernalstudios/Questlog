package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.NbtSaveable;
import org.infernalstudios.questlog.util.Util;

public class Quest implements NbtSaveable, WithDisplayData<QuestDisplayData> {

  private final QuestDisplayData display;
  public final List<Objective> triggers;
  public final List<Objective> objectives;
  public final List<Reward> rewards;

  private final ResourceLocation id;
  public final QuestManager manager;

  public boolean hasSentCompletion = false;
  public boolean hasSentTrigger = false;

  public Quest(
    QuestDisplayData display,
    List<Objective> triggers,
    List<Objective> objectives,
    List<Reward> rewards,
    ResourceLocation id,
    QuestManager manager
  ) {
    this.display = display;
    this.triggers = triggers;
    this.objectives = objectives;
    this.rewards = rewards;
    this.id = id;
    this.manager = manager;

    this.triggers.forEach(trigger -> {
      trigger.setParent(this);
      if (!this.manager.isClient()) {
        trigger.registerEventListeners(Questlog.EVENTS);
      }
    });
    this.objectives.forEach(objective -> {
      objective.setParent(this);
      if (!this.manager.isClient()) {
        objective.registerEventListeners(Questlog.EVENTS);
      }
    });
    this.rewards.forEach(reward -> reward.setParent(this));
    display.setQuest(this);
  }

  public ResourceLocation getId() {
    return this.id;
  }

  @Override
  public QuestDisplayData getDisplay() {
    return this.display;
  }

  public boolean isTriggered() {
    for (Objective trigger : this.triggers) {
      if (!trigger.isCompleted()) {
        return false;
      }
    }

    return true;
  }

  public boolean isCompleted() {
    for (Objective objective : this.objectives) {
      if (!objective.isCompleted()) {
        return false;
      }
    }

    return true;
  }

  public boolean isRewarded() {
    for (Reward reward : this.rewards) {
      if (!reward.hasRewarded()) {
        return false;
      }
    }

    return true;
  }

  public void markForUpdate() {
    this.manager.sync(this.id);
  }

  /** {@inheritDoc} */
  @Override
  public void writeInitialData(CompoundTag data) {
    data.putBoolean("completed", this.hasSentCompletion);
    data.putBoolean("triggered", this.hasSentTrigger);

    data.put(
      "triggers",
      Util.toNbtList(this.triggers, trigger -> {
        CompoundTag tag = new CompoundTag();
        trigger.writeInitialData(tag);
        return tag;
      })
    );

    data.put(
      "objectives",
      Util.toNbtList(this.objectives, objective -> {
        CompoundTag tag = new CompoundTag();
        objective.writeInitialData(tag);
        return tag;
      })
    );

    data.put(
      "rewards",
      Util.toNbtList(this.rewards, reward -> {
        CompoundTag tag = new CompoundTag();
        reward.writeInitialData(tag);
        return tag;
      })
    );
  }

  /** {@inheritDoc} */
  @Override
  public void deserialize(CompoundTag data) {
    this.hasSentCompletion = data.getBoolean("completed");
    this.hasSentTrigger = data.getBoolean("triggered");

    List<Tag> triggerData = data.getList("triggers", Tag.TAG_COMPOUND);
    for (int i = 0; i < triggerData.size(); i++) {
      this.triggers.get(i).deserialize((CompoundTag) triggerData.get(i));
    }

    List<Tag> objectiveData = data.getList("objectives", Tag.TAG_COMPOUND);
    for (int i = 0; i < objectiveData.size(); i++) {
      this.objectives.get(i).deserialize((CompoundTag) objectiveData.get(i));
    }

    List<Tag> rewardData = data.getList("rewards", Tag.TAG_COMPOUND);
    for (int i = 0; i < rewardData.size(); i++) {
      this.rewards.get(i).deserialize((CompoundTag) rewardData.get(i));
    }
  }

  /** {@inheritDoc} */
  @Override
  public CompoundTag serialize() {
    CompoundTag tag = new CompoundTag();
    tag.putBoolean("completed", this.hasSentCompletion);
    tag.putBoolean("triggered", this.hasSentTrigger);
    tag.put("triggers", Util.toNbtList(this.triggers, Objective::serialize));
    tag.put("objectives", Util.toNbtList(this.objectives, Objective::serialize));
    tag.put("rewards", Util.toNbtList(this.rewards, Reward::serialize));
    return tag;
  }

  public static Quest create(JsonObject definition, ResourceLocation id, QuestManager manager) {
    QuestDisplayData display = new QuestDisplayData(JsonUtils.getOrDefault(definition, "display", new JsonObject()));
    List<Objective> triggers = new ArrayList<>();
    List<Objective> objectives = new ArrayList<>();
    List<Reward> rewards = new ArrayList<>();

    for (JsonElement triggerElement : JsonUtils.getOrDefault(definition, "triggers", new JsonArray())) {
      if (!triggerElement.isJsonObject()) {
        throw new IllegalStateException("Trigger must be an object");
      }
      JsonObject trigger = triggerElement.getAsJsonObject();
      Objective triggerType = QuestObjectiveRegistry.create(trigger);
      triggers.add(triggerType);
    }

    for (JsonElement objectiveElement : JsonUtils.getOrDefault(definition, "objectives", new JsonArray())) {
      if (!objectiveElement.isJsonObject()) {
        throw new IllegalStateException("Objective must be an object");
      }
      JsonObject objective = objectiveElement.getAsJsonObject();
      Objective objectiveType = QuestObjectiveRegistry.create(objective);
      objectives.add(objectiveType);
    }

    for (JsonElement rewardElement : JsonUtils.getOrDefault(definition, "rewards", new JsonArray())) {
      if (!rewardElement.isJsonObject()) {
        throw new IllegalStateException("Reward must be an object");
      }
      JsonObject reward = rewardElement.getAsJsonObject();
      Reward rewardType = QuestRewardRegistry.create(reward);
      rewards.add(rewardType);
    }

    return new Quest(display, triggers, objectives, rewards, id, manager);
  }
}

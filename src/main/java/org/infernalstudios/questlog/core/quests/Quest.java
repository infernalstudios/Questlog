package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.NbtSaveable;
import org.infernalstudios.questlog.util.Util;

import java.util.*;

public class Quest implements NbtSaveable, WithDisplayData<QuestDisplayData> {
  private final QuestDisplayData display;
  private final List<Objective> triggers;
  public final List<Objective> objectives;
  public final List<Reward> rewards;

  private final ResourceLocation id;
  public final QuestManager manager;


  public Quest(QuestDisplayData display, List<Objective> triggers, List<Objective> objectives, List<Reward> rewards, ResourceLocation id, QuestManager manager) {
    this.display = display;
    display.setQuest(this);
    this.triggers = triggers;
    this.objectives = objectives;
    this.rewards = rewards;
    this.id = id;
    this.manager = manager;

    this.triggers.forEach(trigger -> trigger.setParent(this));
    this.objectives.forEach(objective -> objective.setParent(this));
    this.rewards.forEach(reward -> reward.setParent(this));
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

  public boolean isComplete() {
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
    data.put("triggers", Util.toNbtList(this.triggers, trigger -> {
      CompoundTag tag = new CompoundTag();
      trigger.writeInitialData(tag);
      return tag;
    }));

    data.put("objectives", Util.toNbtList(this.objectives, objective -> {
      CompoundTag tag = new CompoundTag();
      objective.writeInitialData(tag);
      return tag;
    }));

    data.put("rewards", Util.toNbtList(this.rewards, reward -> {
      CompoundTag tag = new CompoundTag();
      reward.writeInitialData(tag);
      return tag;
    }));
  }

  /** {@inheritDoc} */
  @Override
  public void deserialize(CompoundTag data) {
    List<Tag> triggerData = Util.readNbtList(data.getCompound("triggers"));
    for (int i = 0; i < triggerData.size(); i++) {
      this.triggers.get(i).deserialize((CompoundTag) triggerData.get(i));
    }

    List<Tag> objectiveData = Util.readNbtList(data.getCompound("objectives"));
    for (int i = 0; i < objectiveData.size(); i++) {
      this.objectives.get(i).deserialize((CompoundTag) objectiveData.get(i));
    }

    List<Tag> rewardData = Util.readNbtList(data.getCompound("rewards"));
    for (int i = 0; i < rewardData.size(); i++) {
      this.rewards.get(i).deserialize((CompoundTag) rewardData.get(i));
    }
  }

  /** {@inheritDoc} */
  @Override
  public CompoundTag serialize() {
    CompoundTag tag = new CompoundTag();
    tag.put("triggers", Util.toNbtList(this.triggers, Objective::serialize));
    tag.put("objectives", Util.toNbtList(this.objectives, Objective::serialize));
    tag.put("rewards", Util.toNbtList(this.rewards, Reward::serialize));
    return tag;
  }

  public static Quest create(JsonObject definition, ResourceLocation id, QuestManager manager) {
    QuestDisplayData display = new QuestDisplayData(definition.get("display").getAsJsonObject());
    List<Objective> triggers = new ArrayList<>();
    List<Objective> objectives = new ArrayList<>();
    List<Reward> rewards = new ArrayList<>();

    for (JsonElement triggerElement : definition.get("triggers").getAsJsonArray()) {
      JsonObject trigger = triggerElement.getAsJsonObject();
      Objective triggerType = QuestTypeRegistry.create(trigger);
      triggers.add(triggerType);
    }

    for (JsonElement objectiveElement : definition.get("objectives").getAsJsonArray()) {
      JsonObject objective = objectiveElement.getAsJsonObject();
      Objective objectiveType = QuestTypeRegistry.create(objective);
      objectives.add(objectiveType);
    }

    for (JsonElement rewardElement : definition.get("rewards").getAsJsonArray()) {
      JsonObject reward = rewardElement.getAsJsonObject();
      Reward rewardType = QuestRewardRegistry.create(reward);
      rewards.add(rewardType);
    }

    return new Quest(display, triggers, objectives, rewards, id, manager);
  }
}

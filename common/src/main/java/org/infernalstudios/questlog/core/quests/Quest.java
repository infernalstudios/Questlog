package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.util.ArrayList;
import java.util.List;

public class Quest implements NbtSaveable, WithDisplayData<QuestDisplayData> {

    public final List<Objective> prerequisites;
    public final List<Objective> objectives;
    public final List<Objective> failureConditions;
    public final List<Reward> rewards;
    public final QuestManager manager;
    private final QuestDisplayData display;
    private final ResourceLocation id;
    public boolean hasSentCompletion = false;
    public boolean hasSentTrigger = false;
    private boolean repeatable = false;
    private boolean global = false;

    public Quest(
            QuestDisplayData display,
            List<Objective> prerequisites,
            List<Objective> objectives,
            List<Objective> failureConditions,
            List<Reward> rewards,
            ResourceLocation id,
            QuestManager manager,
            boolean repeatable,
            boolean global
    ) {
        this.display = display;
        this.prerequisites = prerequisites;
        this.objectives = objectives;
        this.failureConditions = failureConditions;
        this.rewards = rewards;
        this.id = id;
        this.manager = manager;
        this.repeatable = repeatable;
        this.global = global;

        if (this.prerequisites.isEmpty()) {
            this.hasSentTrigger = true;
        }

        this.prerequisites.forEach(prerequisite -> {
            prerequisite.markAsPrerequisite();
            prerequisite.setParent(this);
            if (!this.manager.isClient()) {
                prerequisite.registerEventListeners();
            }
        });
        this.objectives.forEach(objective -> {
            objective.setParent(this);
            if (!this.manager.isClient()) {
                objective.registerEventListeners();
            }
        });
        this.failureConditions.forEach(failureCondition -> {
            failureCondition.setParent(this);
            if (!this.manager.isClient()) {
                failureCondition.registerEventListeners();
            }
        });
        this.rewards.forEach(reward -> reward.setParent(this));
        display.setQuest(this);
    }

    public static Quest create(JsonObject definition, ResourceLocation id, QuestManager manager) {
        QuestDisplayData display = new QuestDisplayData(definition);
        List<Objective> prerequisites = new ArrayList<>();
        List<Objective> objectives = new ArrayList<>();
        List<Objective> failureConditions = new ArrayList<>();
        List<Reward> rewards = new ArrayList<>();

        JsonArray reqArray = definition.has("prerequisites") ? definition.getAsJsonArray("prerequisites")
                : (definition.has("requirements") ? definition.getAsJsonArray("requirements") : new JsonArray());
        for (JsonElement reqElement : reqArray) {
            if (reqElement.isJsonObject()) {
                prerequisites.add(QuestObjectiveRegistry.create(reqElement.getAsJsonObject()));
            }
        }

        for (JsonElement objectiveElement : JsonUtils.getOrDefault(definition, "objectives", new JsonArray())) {
            if (objectiveElement.isJsonObject()) {
                objectives.add(QuestObjectiveRegistry.create(objectiveElement.getAsJsonObject()));
            }
        }

        for (JsonElement failElement : JsonUtils.getOrDefault(definition, "failures", new JsonArray())) {
            if (failElement.isJsonObject()) {
                failureConditions.add(QuestObjectiveRegistry.create(failElement.getAsJsonObject()));
            }
        }

        for (JsonElement rewardElement : JsonUtils.getOrDefault(definition, "rewards", new JsonArray())) {
            if (rewardElement.isJsonObject()) {
                rewards.add(QuestRewardRegistry.create(rewardElement.getAsJsonObject()));
            }
        }

        boolean repeatable = JsonUtils.getOrDefault(definition, "repeatable", false);
        boolean global = JsonUtils.getOrDefault(definition, "global", false);

        return new Quest(display, prerequisites, objectives, failureConditions, rewards, id, manager, repeatable, global);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public void resetProgress() {
        this.prerequisites.forEach(trigger -> trigger.forceSetUnits(0));
        this.objectives.forEach(obj -> obj.forceSetUnits(0));
        this.failureConditions.forEach(obj -> obj.forceSetUnits(0));
        this.rewards.forEach(Reward::revokeReward);
        this.hasSentTrigger = this.prerequisites.isEmpty();
        this.hasSentCompletion = false;
        this.markForUpdate();
    }

    @Override
    public QuestDisplayData getDisplay() {
        return this.display;
    }

    public boolean isTriggered() {
        for (Objective req : this.prerequisites) {
            if (!req.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFailed() {
        return !this.failureConditions.isEmpty() && this.failureConditions.stream().anyMatch(Objective::isCompleted);
    }

    public boolean isCompleted() {
        if (this.isFailed()) return false;

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

    @Override
    public void writeInitialData(CompoundTag data) {
        data.putBoolean("completed", this.hasSentCompletion);
        data.putBoolean("triggered", this.hasSentTrigger);
        data.putBoolean("repeatable", this.repeatable);
        data.putBoolean("global", this.global);

        data.put(
                "prerequisites",
                Util.toNbtList(this.prerequisites, prerequisite -> {
                    CompoundTag tag = new CompoundTag();
                    prerequisite.writeInitialData(tag);
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
                "failures",
                Util.toNbtList(this.failureConditions, condition -> {
                    CompoundTag tag = new CompoundTag();
                    condition.writeInitialData(tag);
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

    @Override
    public void deserialize(CompoundTag data) {
        this.hasSentCompletion = data.getBoolean("completed");
        this.hasSentTrigger = data.getBoolean("triggered");
        if (data.contains("repeatable")) {
            this.repeatable = data.getBoolean("repeatable");
        }
        if (data.contains("global")) {
            this.global = data.getBoolean("global");
        }

        if (this.prerequisites.isEmpty()) {
            this.hasSentTrigger = true;
        }

        List<Tag> reqData = data.contains("prerequisites") ? data.getList("prerequisites", Tag.TAG_COMPOUND)
                : data.getList("requirements", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(reqData.size(), this.prerequisites.size()); i++) {
            this.prerequisites.get(i).deserialize((CompoundTag) reqData.get(i));
        }

        List<Tag> objectiveData = data.getList("objectives", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(objectiveData.size(), this.objectives.size()); i++) {
            this.objectives.get(i).deserialize((CompoundTag) objectiveData.get(i));
        }

        List<Tag> failureData = data.getList("failures", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(failureData.size(), this.failureConditions.size()); i++) {
            this.failureConditions.get(i).deserialize((CompoundTag) failureData.get(i));
        }

        List<Tag> rewardData = data.getList("rewards", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(rewardData.size(), this.rewards.size()); i++) {
            this.rewards.get(i).deserialize((CompoundTag) rewardData.get(i));
        }
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("completed", this.hasSentCompletion);
        tag.putBoolean("triggered", this.hasSentTrigger);
        tag.putBoolean("repeatable", this.repeatable);
        tag.putBoolean("global", this.global);
        tag.put("prerequisites", Util.toNbtList(this.prerequisites, Objective::serialize));
        tag.put("objectives", Util.toNbtList(this.objectives, Objective::serialize));
        tag.put("failures", Util.toNbtList(this.failureConditions, Objective::serialize));
        tag.put("rewards", Util.toNbtList(this.rewards, Reward::serialize));
        return tag;
    }
}
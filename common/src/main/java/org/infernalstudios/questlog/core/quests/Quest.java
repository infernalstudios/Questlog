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

    public final List<Objective> requirements;
    public final List<Objective> objectives;
    public final List<Reward> rewards;
    public final QuestManager manager;
    private final QuestDisplayData display;
    private final ResourceLocation id;
    public boolean hasSentCompletion = false;
    public boolean hasSentTrigger = false;

    public Quest(
            QuestDisplayData display,
            List<Objective> requirements,
            List<Objective> objectives,
            List<Reward> rewards,
            ResourceLocation id,
            QuestManager manager
    ) {
        this.display = display;
        this.requirements = requirements;
        this.objectives = objectives;
        this.rewards = rewards;
        this.id = id;
        this.manager = manager;

        if (this.requirements.isEmpty()) {
            this.hasSentTrigger = true;
        }

        this.requirements.forEach(requirement -> {
            requirement.setParent(this);
            if (!this.manager.isClient()) {
                requirement.registerEventListeners(Questlog.EVENTS);
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

    public static Quest create(JsonObject definition, ResourceLocation id, QuestManager manager) {
        QuestDisplayData display = new QuestDisplayData(definition);
        List<Objective> requirements = new ArrayList<>();
        List<Objective> objectives = new ArrayList<>();
        List<Reward> rewards = new ArrayList<>();

        for (JsonElement reqElement : JsonUtils.getOrDefault(definition, "requirements", new JsonArray())) {
            if (reqElement.isJsonObject()) {
                requirements.add(QuestObjectiveRegistry.create(reqElement.getAsJsonObject()));
            }
        }

        for (JsonElement objectiveElement : JsonUtils.getOrDefault(definition, "objectives", new JsonArray())) {
            if (objectiveElement.isJsonObject()) {
                objectives.add(QuestObjectiveRegistry.create(objectiveElement.getAsJsonObject()));
            }
        }

        for (JsonElement rewardElement : JsonUtils.getOrDefault(definition, "rewards", new JsonArray())) {
            if (rewardElement.isJsonObject()) {
                rewards.add(QuestRewardRegistry.create(rewardElement.getAsJsonObject()));
            }
        }

        return new Quest(display, requirements, objectives, rewards, id, manager);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public QuestDisplayData getDisplay() {
        return this.display;
    }

    public boolean isTriggered() {
        for (Objective req : this.requirements) {
            if (!req.isCompleted()) {
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

    @Override
    public void writeInitialData(CompoundTag data) {
        data.putBoolean("completed", this.hasSentCompletion);
        data.putBoolean("triggered", this.hasSentTrigger);

        data.put(
                "requirements",
                Util.toNbtList(this.requirements, requirement -> {
                    CompoundTag tag = new CompoundTag();
                    requirement.writeInitialData(tag);
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

    @Override
    public void deserialize(CompoundTag data) {
        this.hasSentCompletion = data.getBoolean("completed");
        this.hasSentTrigger = data.getBoolean("triggered");

        if (this.requirements.isEmpty()) {
            this.hasSentTrigger = true;
        }

        List<Tag> reqData = data.getList("requirements", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(reqData.size(), this.requirements.size()); i++) {
            this.requirements.get(i).deserialize((CompoundTag) reqData.get(i));
        }

        List<Tag> objectiveData = data.getList("objectives", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(objectiveData.size(), this.objectives.size()); i++) {
            this.objectives.get(i).deserialize((CompoundTag) objectiveData.get(i));
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
        tag.put("requirements", Util.toNbtList(this.requirements, Objective::serialize));
        tag.put("objectives", Util.toNbtList(this.objectives, Objective::serialize));
        tag.put("rewards", Util.toNbtList(this.rewards, Reward::serialize));
        return tag;
    }
}
package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.QuestRewardRegistry;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A reward that rolls one (or more) of its entries at random, weighted.
 * Unlike {@link ChoiceReward} the player does not pick. The roll happens when the reward is
 * claimed, and the outcome is persisted.
 */
public class RandomReward extends Reward {

    private final List<Reward> entries = new ArrayList<>();
    private final List<Double> weights = new ArrayList<>();
    private final double emptyWeight;
    private final int rollCount;
    private final List<Integer> rolledIndices = new ArrayList<>();

    public RandomReward(JsonObject definition) {
        super(definition);
        this.emptyWeight = Math.max(0, JsonUtils.getOrDefault(definition, "empty_weight", 0d));
        this.rollCount = Math.max(1, JsonUtils.getOrDefault(definition, "roll_count", 1));

        for (JsonElement element : JsonUtils.getOrDefault(definition, "entries", new JsonArray())) {
            if (!element.isJsonObject()) continue;
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("reward") || !entry.get("reward").isJsonObject()) continue;

            this.entries.add(QuestRewardRegistry.create(entry.getAsJsonObject("reward")));
            this.weights.add(Math.max(0, JsonUtils.getOrDefault(entry, "weight", 1d)));
        }
    }

    public List<Reward> getEntries() {
        return this.entries;
    }

    public List<Double> getWeights() {
        return this.weights;
    }

    public double getEmptyWeight() {
        return this.emptyWeight;
    }

    public int getRollCount() {
        return this.rollCount;
    }

    public List<Integer> getRolledIndices() {
        return this.rolledIndices;
    }

    private int roll(ServerPlayer player) {
        double total = this.emptyWeight;
        for (double weight : this.weights) total += weight;
        if (total <= 0) return -1;

        double pick = player.getRandom().nextDouble() * total;
        for (int i = 0; i < this.entries.size(); i++) {
            pick -= this.weights.get(i);
            if (pick < 0) return i;
        }
        return -1;
    }

    @Override
    public void applyReward(ServerPlayer player) {
        if (this.rolledIndices.isEmpty()) {
            for (int i = 0; i < this.rollCount; i++) {
                this.rolledIndices.add(this.roll(player));
            }
        }

        for (int index : this.rolledIndices) {
            if (index < 0 || index >= this.entries.size()) continue;
            Reward entry = this.entries.get(index);
            try {
                entry.applyReward(player);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to apply random reward entry {}", index, e);
            }
        }
        super.applyReward(player);
    }

    @Override
    public void revokeReward() {
        super.revokeReward();
        this.rolledIndices.clear();
        for (Reward entry : this.entries) {
            entry.setRewarded(false);
        }
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = super.serialize();
        tag.putIntArray("rolled_indices", this.rolledIndices.stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag data) {
        super.deserialize(data);
        this.rolledIndices.clear();
        for (int i : data.getIntArray("rolled_indices")) {
            this.rolledIndices.add(i);
        }
        if (this.hasRewarded()) {
            for (Reward entry : this.entries) {
                entry.setRewarded(false);
            }
            for (int index : this.rolledIndices) {
                if (index >= 0 && index < this.entries.size()) {
                    this.entries.get(index).setRewarded(true);
                }
            }
        }
    }
}

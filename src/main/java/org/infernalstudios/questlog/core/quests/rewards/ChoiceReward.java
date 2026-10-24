package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.QuestRewardRegistry;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class ChoiceReward extends Reward {

    private final List<Reward> choices;
    private final int pickCount;
    private final List<Integer> selectedIndices = new ArrayList<>();

    public ChoiceReward(JsonObject definition) {
        super(definition);
        this.pickCount = JsonUtils.getOrDefault(definition, "pick_count", 1);
        this.choices = new ArrayList<>();
        for (JsonElement element : JsonUtils.getOrDefault(definition, "choices", new JsonArray())) {
            if (element.isJsonObject()) {
                Reward choice = QuestRewardRegistry.create(element.getAsJsonObject());
                choice.setContainer(this);
                this.choices.add(choice);
            }
        }
    }

    public List<Reward> getChoices() {
        return this.choices;
    }

    public int getPickCount() {
        return this.pickCount;
    }

    public List<Integer> getSelectedIndicesList() {
        return this.selectedIndices;
    }

    public void setSelectedIndices(List<Integer> indices) {
        this.selectedIndices.clear();
        if (indices != null) {
            for (int idx : indices) {
                if (idx >= 0 && idx < this.choices.size() && !this.selectedIndices.contains(idx)) {
                    this.selectedIndices.add(idx);
                }
            }
        }
    }

    public boolean isChoiceSelected(Reward choice) {
        int index = this.choices.indexOf(choice);
        return index != -1 && this.selectedIndices.contains(index);
    }

    public void toggleChoice(Reward choice) {
        int index = this.choices.indexOf(choice);
        if (index == -1) return;

        if (this.selectedIndices.contains(index)) {
            this.selectedIndices.remove((Integer) index);
        } else {
            if (this.selectedIndices.size() >= this.pickCount) {
                this.selectedIndices.remove(0);
            }
            this.selectedIndices.add(index);
        }
        if (this.getParent() != null) {
            this.getParent().markForUpdate();
        }
    }

    public boolean canClaim() {
        return this.selectedIndices.size() == this.pickCount;
    }

    @Override
    public void applyReward(ServerPlayer player) {
        for (int index : this.selectedIndices) {
            if (index >= 0 && index < this.choices.size()) {
                this.choices.get(index).applyReward(player);
            }
        }
        for (Reward choice : this.choices) {
            choice.setRewarded(true);
        }
        super.applyReward(player);
    }

    @Override
    public void revokeReward() {
        super.revokeReward();
        for (Reward choice : this.choices) {
            choice.setRewarded(false);
        }
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = super.serialize();
        tag.putIntArray("selected_indices", this.selectedIndices.stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag data) {
        super.deserialize(data);
        this.selectedIndices.clear();
        for (int i : data.getIntArray("selected_indices")) {
            this.selectedIndices.add(i);
        }
        if (this.hasRewarded()) {
            for (Reward choice : this.choices) {
                choice.setRewarded(false);
            }
            for (int index : this.selectedIndices) {
                if (index >= 0 && index < this.choices.size()) {
                    this.choices.get(index).setRewarded(true);
                }
            }
        }
    }
}

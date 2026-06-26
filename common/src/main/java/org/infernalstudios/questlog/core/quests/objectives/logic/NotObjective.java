package org.infernalstudios.questlog.core.quests.objectives.logic;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.QuestObjectiveRegistry;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.List;

public class NotObjective extends Objective {
    private final Objective child;

    public NotObjective(JsonObject definition) {
        super(definition);
        this.child = QuestObjectiveRegistry.create(JsonUtils.getObject(definition, "objective"));
    }

    @Override
    public void setParent(Quest parent) {
        super.setParent(parent);
        this.child.setParent(parent);
    }

    @Override
    public void markAsRequirement() {
        super.markAsRequirement();
        this.child.markAsRequirement();
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        this.child.registerEventListeners();
    }

    @Override
    public boolean isCompleted() {
        return !this.child.isCompleted();
    }

    @Override
    public int getUnits() {
        return isCompleted() ? 1 : 0;
    }

    @Override
    public void setUnits(int units) {
        super.setUnits(units);
        if (units == 0) {
            this.child.setUnits(this.child.getRequiredAmount());
        } else {
            this.child.setUnits(0);
        }
    }

    @Override
    public int getRequiredAmount() {
        return 1;
    }

    @Override
    public void writeInitialData(CompoundTag data) {
        super.writeInitialData(data);
        CompoundTag childTag = new CompoundTag();
        this.child.writeInitialData(childTag);
        data.put("child", childTag);
    }

    @Override
    public void deserialize(CompoundTag data) {
        super.deserialize(data);
        this.child.deserialize(data.getCompound("child"));
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag data = super.serialize();
        data.put("child", this.child.serialize());
        return data;
    }

    @Override
    public List<Objective> getChildren() {
        return List.of(this.child);
    }

    @Override
    public void forceSetUnits(int units) {
        super.forceSetUnits(units);
        if (units == 0) {
            this.child.forceSetUnits(this.child.getRequiredAmount());
        } else {
            this.child.forceSetUnits(0);
        }
    }
}
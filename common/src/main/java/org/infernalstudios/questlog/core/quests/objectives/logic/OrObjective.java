package org.infernalstudios.questlog.core.quests.objectives.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.QuestObjectiveRegistry;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class OrObjective extends Objective {
    private final List<Objective> children = new ArrayList<>();

    public OrObjective(JsonObject definition) {
        super(definition);
        JsonArray objectives = JsonUtils.getOrDefault(definition, "objectives", new JsonArray());
        for (JsonElement element : objectives) {
            if (element.isJsonObject()) {
                this.children.add(QuestObjectiveRegistry.create(element.getAsJsonObject()));
            }
        }
    }

    @Override
    public void setParent(Quest parent) {
        super.setParent(parent);
        for (Objective child : children) {
            child.setParent(parent);
        }
    }

    @Override
    public void markAsPrerequisite() {
        super.markAsPrerequisite();
        for (Objective child : children) {
            child.markAsPrerequisite();
        }
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        for (Objective child : children) {
            child.registerEventListeners();
        }
    }

    @Override
    public boolean isCompleted() {
        for (Objective child : children) {
            if (child.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getUnits() {
        return isCompleted() ? 1 : 0;
    }

    @Override
    public void setUnits(int units) {
        super.setUnits(units);
        if (units == 0) {
            for (Objective child : this.children) {
                child.setUnits(0);
            }
        } else if (!this.children.isEmpty()) {
            this.children.getFirst().setUnits(this.children.getFirst().getRequiredAmount());
        }
    }

    @Override
    public int getRequiredAmount() {
        return 1;
    }

    @Override
    public void writeInitialData(CompoundTag data) {
        super.writeInitialData(data);
        ListTag list = new ListTag();
        for (Objective child : children) {
            CompoundTag childTag = new CompoundTag();
            child.writeInitialData(childTag);
            list.add(childTag);
        }
        data.put("children", list);
    }

    @Override
    public void deserialize(CompoundTag data) {
        super.deserialize(data);
        ListTag list = data.getList("children", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && i < children.size(); i++) {
            children.get(i).deserialize(list.getCompound(i));
        }
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag data = super.serialize();
        ListTag list = new ListTag();
        for (Objective child : children) {
            list.add(child.serialize());
        }
        data.put("children", list);
        return data;
    }

    @Override
    public List<Objective> getChildren() {
        return this.children;
    }

    @Override
    public void forceSetUnits(int units) {
        super.forceSetUnits(units);
        if (units == 0) {
            for (Objective child : this.children) {
                child.forceSetUnits(0);
            }
        } else if (!this.children.isEmpty()) {
            this.children.getFirst().forceSetUnits(this.children.getFirst().getRequiredAmount());
        }
    }
}
package org.infernalstudios.questlog.core.quests.objectives;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.NbtSaveable;
import org.jetbrains.annotations.Nullable;

public abstract class Objective implements NbtSaveable, WithDisplayData<ObjectiveDisplayData> {

    private final ObjectiveDisplayData display;
    private final int requiredAmount;
    private Quest parent;
    private int units;

    public Objective(JsonObject definition) {
        this.requiredAmount = JsonUtils.getOrDefault(definition, "required_amount", 1);
        this.units = 0;

        this.display = new ObjectiveDisplayData(definition);
        this.display.setObjective(this);
    }

    public void registerEventListeners() {
    }

    @Nullable
    public final Quest getParent() {
        return this.parent;
    }

    public void setParent(@Nullable Quest parent) {
        this.parent = parent;
    }

    public int getUnits() {
        return this.units;
    }

    public void setUnits(int units) {
        if (this.getParent() != null && !this.getParent().isTriggered() && !this.getParent().requirements.contains(this)) {
            return;
        }

        this.units = Math.min(units, this.requiredAmount);
        if (this.getParent() != null) {
            this.getParent().markForUpdate();
        }
    }

    public int getRequiredAmount() {
        return this.requiredAmount;
    }

    public boolean isCompleted() {
        return this.units >= this.requiredAmount;
    }

    public boolean isHidden() {
        return false;
    }

    @Override
    public ObjectiveDisplayData getDisplay() {
        return this.display;
    }

    @Override
    public void writeInitialData(CompoundTag data) {
        data.putInt("units", this.units);
    }

    @Override
    public void deserialize(CompoundTag data) {
        this.units = data.getInt("units");
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("units", this.units);
        return tag;
    }

    public boolean isReadObjective() {
        return false;
    }
}
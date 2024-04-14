package org.infernalstudios.questlog.core.quests.objectives;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.util.NbtSaveable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public abstract class Objective implements NbtSaveable, WithDisplayData<ObjectiveDisplayData> {
  @Nullable
  private final ObjectiveDisplayData display;

  @CheckForNull
  private Quest parent;
  private final int totalUnits;
  private int units;

  public Objective(JsonObject definition) {
    this.totalUnits = definition.get("total").getAsInt();
    this.units = 0;

    if (definition.has("display")) {
      this.display = new ObjectiveDisplayData(definition.getAsJsonObject("display"));
      this.display.setObjective(this);
    } else {
      this.display = null;
    }

    this.registerEventListeners(MinecraftForge.EVENT_BUS);
  }

  // This is required due to EVENT_BUS.register(this) does not work for superclasses
  protected void registerEventListeners(IEventBus bus) {
  }

  public final void setParent(Quest parent) {
    this.parent = parent;
  }

  public final Quest getParent() {
    if (this.parent == null) {
      throw new IllegalStateException("QuestType has not been assigned a parent quest");
    }

    return this.parent;
  }

  public final void setUnits(int units) {
    this.units = Math.min(units, this.totalUnits);
    this.getParent().markForUpdate();
  }

  public final int getUnits() {
    return this.units;
  }

  public final int getTotalUnits() {
    return this.totalUnits;
  }

  public final boolean isCompleted() {
    return this.units >= this.totalUnits;
  }

  @Override
  @Nullable
  public ObjectiveDisplayData getDisplay() {
    return this.display;
  }

  /** {@inheritDoc} */
  @Override
  public void writeInitialData(CompoundTag data) {
    data.putInt("units", this.units);
  }

  /** {@inheritDoc} */
  @Override
  public void deserialize(CompoundTag data) {
    this.units = data.getInt("units");
  }

  /** {@inheritDoc} */
  @Override
  public CompoundTag serialize() {
    CompoundTag tag = new CompoundTag();
    tag.putInt("units", this.units);
    return tag;
  }
}

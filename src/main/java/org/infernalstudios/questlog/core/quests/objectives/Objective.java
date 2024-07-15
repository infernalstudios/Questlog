package org.infernalstudios.questlog.core.quests.objectives;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.NbtSaveable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public abstract class Objective implements NbtSaveable, WithDisplayData<ObjectiveDisplayData> {
  private final ObjectiveDisplayData display;

  @CheckForNull
  private Quest parent;
  private final int totalUnits;
  private int units;

  public Objective(JsonObject definition) {
    this.totalUnits = JsonUtils.getOrDefault(definition, "total", 1);
    this.units = 0;

    this.display = new ObjectiveDisplayData(JsonUtils.getOrDefault(definition, "display", new JsonObject()));

    this.registerEventListeners(Questlog.GENERIC_EVENT_BUS);
  }

  // This is required due to EVENT_BUS.register(this) does not work for superclasses
  protected void registerEventListeners(GenericEventBus bus) {
  }

  public final void setParent(Quest parent) {
    this.parent = parent;
  }

  @Nullable
  public final Quest getParent() {
    return this.parent;
  }

  public final void setUnits(int units) {
    this.units = Math.min(units, this.totalUnits);
    if (this.getParent() != null) {
      this.getParent().markForUpdate();
    }
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

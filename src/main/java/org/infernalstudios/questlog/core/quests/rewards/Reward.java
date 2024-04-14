package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.util.NbtSaveable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public abstract class Reward implements NbtSaveable, WithDisplayData<RewardDisplayData> {
  @CheckForNull
  private Quest parent;
  @Nullable
  private final RewardDisplayData display;
  private final boolean isInstant;
  private boolean rewarded = false;

  public Reward(JsonObject definition) {
    if (definition.has("instant")) {
      if (!definition.get("instant").isJsonPrimitive()) {
        throw new IllegalStateException("Reward instant must be a boolean");
      }
      this.isInstant = definition.get("instant").getAsBoolean();
    } else {
      this.isInstant = false;
    }

    if (definition.has("display")) {
      this.display = new RewardDisplayData(definition.getAsJsonObject("display"));
      this.display.setReward(this);
    } else {
      this.display = null;
    }
  }

  public final void setParent(Quest parent) {
    this.parent = parent;
  }

  public Quest getParent() {
    if (this.parent == null) {
      throw new IllegalStateException("Reward has no parent quest");
    }

    return this.parent;
  }

  @Override
  @Nullable
  public RewardDisplayData getDisplay() {
    return this.display;
  }

  public boolean rewardsInstantly() {
    // TODO
    return this.isInstant;
  }

  public void applyReward(ServerPlayer player) {
    this.rewarded = true;
    this.getParent().markForUpdate();
  }

  public boolean hasRewarded() {
    return this.rewarded;
  }

  /** {@inheritDoc} */
  @Override
  public void writeInitialData(CompoundTag data) {
    data.putBoolean("rewarded", false);
  }

  /** {@inheritDoc} */
  @Override
  public CompoundTag serialize() {
    CompoundTag tag = new CompoundTag();
    tag.putBoolean("rewarded", this.rewarded);
    return tag;
  }

  /** {@inheritDoc} */
  @Override
  public void deserialize(CompoundTag data) {
    this.rewarded = data.getBoolean("rewarded");
  }
}

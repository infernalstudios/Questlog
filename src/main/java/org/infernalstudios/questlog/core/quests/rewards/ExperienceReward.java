package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

public class ExperienceReward extends Reward {
  private final int experience;
  private final boolean levels;

  public ExperienceReward(JsonObject definition) {
    super(definition);

    this.experience = definition.get("experience").getAsInt();
    if (definition.has("levels")) {
      this.levels = definition.get("levels").getAsBoolean();
    } else {
      this.levels = false;
    }
  }

  @Override
  public void applyReward(ServerPlayer player) {
    if (this.levels) {
      player.giveExperienceLevels(this.experience);
    } else {
      player.giveExperiencePoints(this.experience);
    }

    super.applyReward(player);
  }
}

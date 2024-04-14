package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.Texture;

import javax.annotation.Nullable;

public class ObjectiveDisplayData {
  @Nullable
  private Objective objective;
  private final String name;
  private final boolean translatable;
  @Nullable
  private final ResourceLocation icon;

  public ObjectiveDisplayData(JsonObject data) {
    this.name = data.get("name").getAsString();
    this.translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    this.icon = data.has("icon") ? new ResourceLocation(data.get("icon").getAsString()) : null;
  }

  public void setObjective(Objective objective) {
    this.objective = objective;
  }

  public Component getName() {
    return this.translatable ? Component.translatable(this.name) : Component.literal(this.name);
  }

  public Component getProgress(int taskIndex) {
    if (this.objective == null) {
      throw new IllegalStateException("QuestTypeDisplayData has not been assigned a quest type");
    }

    return this.objective.isCompleted() ?
      Component.translatable("questlog.objective.completed") :
      this.objective.getTotalUnits() > 1 ?
        Component.translatable("questlog.objective.in_progress", this.objective.getUnits(), this.objective.getTotalUnits()) :
        Component.translatable("questlog.objective.in_progress_singular");
  }

  @Nullable
  public Texture getIcon() {
    return new Texture(this.icon, 16, 16, 0, 0, 16, 16);
  }
}

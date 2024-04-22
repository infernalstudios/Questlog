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
  @Nullable
  private final Texture icon;
  private final Component name;

  public ObjectiveDisplayData(JsonObject data) {
    boolean translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    String name = data.get("name").getAsString();
    String icon = data.has("icon") ? data.get("icon").getAsString() : null;

    this.name = translatable ? Component.translatable(name) : Component.literal(name);
    this.icon = icon != null ? new Texture(new ResourceLocation(icon), 16, 16, 0, 0, 16, 16) : null;
  }

  public void setObjective(Objective objective) {
    this.objective = objective;
  }

  public Component getName() {
    return this.name;
  }

  public Component getProgress() {
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
    return this.icon;
  }
}

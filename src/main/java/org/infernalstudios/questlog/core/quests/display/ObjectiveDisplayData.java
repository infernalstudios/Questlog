package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Renderable;

public class ObjectiveDisplayData {

  @Nullable
  private Objective objective;

  @Nullable
  private final Renderable icon;

  private final Component name;

  public ObjectiveDisplayData(JsonObject data) {
    String name = JsonUtils.getOrDefault(data, "name", (String) null);

    if (name == null) {
      this.name = Component.translatable("questlog.objective.default");
    } else {
      this.name = JsonUtils.getOrDefault(data, "translatable", false) ? Component.translatable(name) : Component.literal(name);
    }

    this.icon = JsonUtils.getIcon(data, "icon");
  }

  public void setObjective(Objective objective) {
    this.objective = objective;
  }

  public Component getName() {
    return this.name;
  }

  public boolean isCompleted() {
    if (this.objective == null) {
      throw new IllegalStateException("ObjectiveDisplayData has not been assigned a quest type");
    }

    return this.objective.isCompleted();
  }

  public Component getProgress() {
    if (this.objective == null) {
      throw new IllegalStateException("ObjectiveDisplayData has not been assigned a quest type");
    }

    return this.isCompleted()
      ? Component.translatable("questlog.objective.completed")
      : this.objective.getTotalUnits() > 1
        ? Component.translatable("questlog.objective.in_progress", this.objective.getUnits(), this.objective.getTotalUnits())
        : Component.translatable("questlog.objective.in_progress_singular");
  }

  @Nullable
  public Renderable getIcon() {
    return this.icon;
  }
}

package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.texture.ItemRenderable;
import org.infernalstudios.questlog.util.texture.Renderable;
import org.infernalstudios.questlog.util.texture.Texture;

import javax.annotation.Nullable;

public class ObjectiveDisplayData {
  @Nullable
  private Objective objective;
  @Nullable
  private final Renderable icon;
  private final Component name;

  public ObjectiveDisplayData(JsonObject data) {
    boolean translatable = data.has("translatable") ? data.get("translatable").getAsBoolean() : false;
    String name = data.get("name").getAsString();
    JsonObject icon = data.has("icon") ? data.get("icon").getAsJsonObject() : null;

    this.name = translatable ? Component.translatable(name) : Component.literal(name);
    if (icon != null) {
      if (icon.has("texture")) {
        this.icon = new Texture(new ResourceLocation(icon.get("texture").getAsString()), 16, 16, 0, 0, 16, 16);
      } else if (icon.has("item")) {
        this.icon = new ItemRenderable(new ResourceLocation(icon.get("item").getAsString()));
      } else {
        this.icon = null;
      }
    } else {
      this.icon = null;
    }
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

    return this.isCompleted() ?
      Component.translatable("questlog.objective.completed") :
      this.objective.getTotalUnits() > 1 ?
        Component.translatable("questlog.objective.in_progress", this.objective.getUnits(), this.objective.getTotalUnits()) :
        Component.translatable("questlog.objective.in_progress_singular");
  }

  @Nullable
  public Renderable getIcon() {
    return this.icon;
  }
}

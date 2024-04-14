package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

import javax.annotation.Nullable;

public abstract class AbstractEntityObjective extends Objective {
  private final ResourceLocation entity;
  @Nullable
  private EntityType<?> cachedEntity = null;

  public AbstractEntityObjective(JsonObject definition) {
    super(definition);

    this.entity = new ResourceLocation(definition.get("entity").getAsString());
  }

  protected EntityType<?> getEntity() {
    if (this.cachedEntity == null) {
      this.cachedEntity = ForgeRegistries.ENTITY_TYPES.getValue(this.entity);
    }

    return this.cachedEntity;
  }
}

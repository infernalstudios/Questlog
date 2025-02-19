package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.CachedRegistryPredicate;
import org.infernalstudios.questlog.util.JsonUtils;

public abstract class AbstractEntityObjective extends Objective {

  private final CachedRegistryPredicate<EntityType<?>> entity;

  public AbstractEntityObjective(JsonObject definition) {
    super(definition);
    this.entity = new CachedRegistryPredicate<>(
      JsonUtils.getString(definition, "entity"),
      BuiltInRegistries.ENTITY_TYPE,
      Object::equals,
      (tag, entity) -> entity.is(tag)
    );
  }

  protected boolean test(EntityType<?> entity) {
    return this.entity.test(entity);
  }

  protected boolean test(Entity entity) {
    return this.test(entity.getType());
  }
}

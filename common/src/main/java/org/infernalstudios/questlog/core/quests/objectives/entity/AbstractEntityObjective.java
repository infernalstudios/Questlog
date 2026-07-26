package org.infernalstudios.questlog.core.quests.objectives.entity;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.EntityMatcher;

public abstract class AbstractEntityObjective extends Objective {

    private final EntityMatcher entityMatcher;

    public AbstractEntityObjective(JsonObject definition) {
        super(definition);
        this.entityMatcher = EntityMatcher.fromDefinition(definition);
    }

    protected boolean test(EntityType<?> entity) {
        return this.entityMatcher.test(entity);
    }

    protected boolean test(Entity entity) {
        return this.entityMatcher.test(entity);
    }
}

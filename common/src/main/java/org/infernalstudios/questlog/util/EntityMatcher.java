package org.infernalstudios.questlog.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.Nullable;

public class EntityMatcher {

    @Nullable
    private final CachedRegistryPredicate<EntityType<?>> entityPredicate;
    @Nullable
    private final String customName;
    @Nullable
    private final JsonElement predicateJson;

    private EntityPredicate cachedEntityPredicate;
    private boolean failedParsingPredicate = false;

    public EntityMatcher(@Nullable JsonElement entityElement, @Nullable JsonObject definition) {
        String name = null;
        JsonElement predElement = null;

        if (definition != null) {
            if (definition.has("custom_name")) {
                name = definition.get("custom_name").getAsString();
            } else if (definition.has("entity_name")) {
                name = definition.get("entity_name").getAsString();
            }
            if (definition.has("predicate")) {
                predElement = definition.get("predicate");
            }
        }

        if (entityElement != null && !entityElement.isJsonNull()) {
            if (entityElement.isJsonPrimitive() && entityElement.getAsJsonPrimitive().isString()) {
                this.entityPredicate = new CachedRegistryPredicate<>(
                        entityElement.getAsString(),
                        BuiltInRegistries.ENTITY_TYPE,
                        Object::equals,
                        (tag, entity) -> entity.is(tag)
                );
            } else if (entityElement.isJsonObject()) {
                JsonObject entityObj = entityElement.getAsJsonObject();
                String idStr = entityObj.has("id") ? JsonUtils.getString(entityObj, "id") : JsonUtils.getString(entityObj, "type");
                this.entityPredicate = new CachedRegistryPredicate<>(
                        idStr,
                        BuiltInRegistries.ENTITY_TYPE,
                        Object::equals,
                        (tag, entity) -> entity.is(tag)
                );

                if (entityObj.has("custom_name")) {
                    name = entityObj.get("custom_name").getAsString();
                } else if (entityObj.has("entity_name")) {
                    name = entityObj.get("entity_name").getAsString();
                } else if (entityObj.has("name")) {
                    name = entityObj.get("name").getAsString();
                }

                if (entityObj.has("predicate")) {
                    predElement = entityObj.get("predicate");
                }
            } else {
                this.entityPredicate = null;
            }
        } else {
            this.entityPredicate = null;
        }

        this.customName = name;
        this.predicateJson = predElement;
    }

    public static EntityMatcher fromDefinition(JsonObject definition) {
        JsonElement entityElement = definition.get("entity");
        return new EntityMatcher(entityElement, definition);
    }

    public boolean test(EntityType<?> entityType) {
        return this.entityPredicate == null || this.entityPredicate.test(entityType);
    }

    public boolean test(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (this.entityPredicate != null && !this.entityPredicate.test(entity.getType())) {
            return false;
        }
        if (this.customName != null && !this.customName.isEmpty()) {
            if (!entity.hasCustomName() || !this.customName.equals(entity.getCustomName().getString())) {
                return false;
            }
        }
        if (this.predicateJson != null && !this.predicateJson.isJsonNull()) {
            EntityPredicate predicate = getEntityPredicate();
            if (predicate != null && entity.level() instanceof ServerLevel serverLevel) {
                return predicate.matches(serverLevel, entity.position(), entity);
            }
        }
        return true;
    }

    @Nullable
    private EntityPredicate getEntityPredicate() {
        if (this.cachedEntityPredicate == null && !this.failedParsingPredicate) {
            try {
                this.cachedEntityPredicate = EntityPredicate.fromJson(this.predicateJson);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse EntityPredicate from JSON: {}", this.predicateJson, e);
                this.failedParsingPredicate = true;
            }
        }
        return this.cachedEntityPredicate;
    }
}

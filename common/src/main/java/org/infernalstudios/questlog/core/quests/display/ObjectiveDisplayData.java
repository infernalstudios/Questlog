package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.Nullable;

public class ObjectiveDisplayData {

    @Nullable
    private final Blittable icon;
    private final Component name;
    @Nullable
    private Objective objective;
    private int indentLevel = 0;

    public ObjectiveDisplayData(JsonObject data) {
        String name = JsonUtils.getOrDefault(data, "name", (String) null);

        if (name == null) {
            this.name = this.generateSmartName(data);
        } else {
            this.name = JsonUtils.getOrDefault(data, "translatable", false) ? Component.translatable(name) : Component.literal(name);
        }

        this.icon = JsonUtils.getIcon(data, "icon");
    }

    private Component generateSmartName(JsonObject data) {
        String typeStr = JsonUtils.getOrDefault(data, "type", "");
        if (typeStr.isEmpty()) return Component.translatable("questlog.objective.default");

        if (!typeStr.contains(":")) typeStr = "questlog:" + typeStr;
        ResourceLocation type = ResourceLocation.tryParse(typeStr);

        if (type != null) {
            String path = type.getPath();

            if (data.has("item")) {
                String itemStr = null;
                if (data.get("item").isJsonPrimitive()) {
                    itemStr = data.get("item").getAsString();
                } else if (data.get("item").isJsonObject()) {
                    JsonObject itemObj = data.getAsJsonObject("item");
                    if (itemObj.has("id") && itemObj.get("id").isJsonPrimitive()) {
                        itemStr = itemObj.get("id").getAsString();
                    } else if (itemObj.has("item") && itemObj.get("item").isJsonPrimitive()) {
                        itemStr = itemObj.get("item").getAsString();
                    }
                }
                if (itemStr != null && !itemStr.startsWith("#")) {
                    ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
                    if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                        return Component.translatable("questlog.objective.default." + path, BuiltInRegistries.ITEM.get(itemId).getDescription());
                    }
                }
            } else if (data.has("block") && data.get("block").isJsonPrimitive()) {
                String blockStr = data.get("block").getAsString();
                if (!blockStr.startsWith("#")) {
                    ResourceLocation blockId = ResourceLocation.tryParse(blockStr);
                    if (blockId != null && BuiltInRegistries.BLOCK.containsKey(blockId)) {
                        return Component.translatable("questlog.objective.default." + path, BuiltInRegistries.BLOCK.get(blockId).getName());
                    }
                }
            } else if (data.has("entity")) {
                String entityStr = null;
                if (data.get("entity").isJsonPrimitive()) {
                    entityStr = data.get("entity").getAsString();
                } else if (data.get("entity").isJsonObject()) {
                    JsonObject entityObj = data.getAsJsonObject("entity");
                    if (entityObj.has("id") && entityObj.get("id").isJsonPrimitive()) {
                        entityStr = entityObj.get("id").getAsString();
                    } else if (entityObj.has("type") && entityObj.get("type").isJsonPrimitive()) {
                        entityStr = entityObj.get("type").getAsString();
                    }
                }
                if (entityStr != null && !entityStr.startsWith("#")) {
                    ResourceLocation entityId = ResourceLocation.tryParse(entityStr);
                    if (entityId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                        return Component.translatable("questlog.objective.default." + path, BuiltInRegistries.ENTITY_TYPE.get(entityId).getDescription());
                    }
                }
            }

            return Component.translatable("questlog.objective.default." + type.getNamespace() + "." + path);
        }

        return Component.translatable("questlog.objective.default");
    }

    public int getIndentLevel() {
        return this.indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public void setObjective(@Nullable Objective objective) {
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
                : this.objective.getRequiredAmount() > 1
                ? Component.translatable("questlog.objective.in_progress", this.objective.getUnits(), this.objective.getRequiredAmount())
                : Component.translatable("questlog.objective.in_progress_singular");
    }

    @Nullable
    public Blittable getIcon() {
        return this.icon;
    }
}
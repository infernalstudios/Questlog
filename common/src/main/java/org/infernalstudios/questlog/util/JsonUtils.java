package org.infernalstudios.questlog.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.core.quests.rewards.ItemReward;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.ItemRenderable;
import org.infernalstudios.questlog.util.texture.Texture;
import org.jetbrains.annotations.Nullable;

public class JsonUtils {

    public static String getOrDefault(JsonObject obj, String key, String defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + key + " must be a string");
            }
            return obj.get(key).getAsString();
        }
        return defaultValue;
    }

    public static int getOrDefault(JsonObject obj, String key, int defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + key + " must be a number");
            }
            return obj.get(key).getAsInt();
        }
        return defaultValue;
    }

    public static double getOrDefault(JsonObject obj, String key, double defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + key + " must be a number");
            }
            return obj.get(key).getAsDouble();
        }
        return defaultValue;
    }

    public static boolean getOrDefault(JsonObject obj, String key, boolean defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + key + " must be a boolean");
            }
            return obj.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    public static JsonObject getOrDefault(JsonObject obj, String key, JsonObject defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonObject()) {
                throw new IllegalArgumentException("Field " + key + " must be an object");
            }
            return obj.getAsJsonObject(key);
        }
        return defaultValue;
    }

    public static JsonArray getOrDefault(JsonObject obj, String key, JsonArray defaultValue) {
        if (obj.has(key)) {
            if (!obj.get(key).isJsonArray()) {
                throw new IllegalArgumentException("Field " + key + " must be an array");
            }
            return obj.getAsJsonArray(key);
        }
        return defaultValue;
    }

    @Nullable
    public static Blittable getIcon(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return null;

        JsonElement element = obj.get(key);

        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new ItemRenderable(ResourceLocation.parse(element.getAsString()));
        }

        if (element.isJsonObject()) {
            return getIcon(element.getAsJsonObject());
        }

        throw new IllegalArgumentException("Field " + key + " must be a string (item ID) or an icon object");
    }

    @Nullable
    public static Blittable getIcon(@Nullable JsonObject icon) {
        if (icon != null) {
            if (icon.has("texture")) {
                if (!icon.get("texture").isJsonPrimitive()) {
                    throw new IllegalArgumentException("Field icon.texture must be a string");
                }
                return new Texture(ResourceLocation.parse(JsonUtils.getString(icon, "texture")), 16, 16, 0, 0, 16, 16);
            } else if (icon.has("item")) {
                if (icon.get("item").isJsonPrimitive()) {
                    return new ItemRenderable(ResourceLocation.parse(JsonUtils.getString(icon, "item")));
                } else if (icon.get("item").isJsonObject()) {
                    ItemStack parsed = ItemReward.parseItemStack(icon.get("item"), null);
                    if (!parsed.isEmpty()) {
                        return new ItemRenderable(parsed);
                    }
                } else {
                    throw new IllegalArgumentException("Field icon.item must be a string or item object");
                }
            }
        }
        return null;
    }

    public static String getString(JsonObject obj, String name) {
        if (obj.has(name)) {
            if (!obj.get(name).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + name + " must be a string");
            }
            return obj.get(name).getAsString();
        } else {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
    }

    public static int getInt(JsonObject obj, String name) {
        if (obj.has(name)) {
            if (!obj.get(name).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + name + " must be an number");
            }
            return obj.get(name).getAsInt();
        } else {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
    }

    public static boolean getBoolean(JsonObject obj, String name) {
        if (obj.has(name)) {
            if (!obj.get(name).isJsonPrimitive()) {
                throw new IllegalArgumentException("Field " + name + " must be a boolean");
            }
            return obj.get(name).getAsBoolean();
        } else {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
    }

    public static JsonObject getObject(JsonObject obj, String name) {
        if (obj.has(name)) {
            if (!obj.get(name).isJsonObject()) {
                throw new IllegalArgumentException("Field " + name + " must be an object");
            }
            return obj.getAsJsonObject(name);
        } else {
            throw new IllegalArgumentException("Missing required field: " + name);
        }
    }
}

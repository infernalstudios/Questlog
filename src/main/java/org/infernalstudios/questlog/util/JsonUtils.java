package org.infernalstudios.questlog.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.util.texture.ItemRenderable;
import org.infernalstudios.questlog.util.texture.Renderable;
import org.infernalstudios.questlog.util.texture.Texture;

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
  public static Renderable getIcon(JsonObject obj, String key) {
    if (obj.has(key)) {
      if (!obj.get(key).isJsonObject()) {
        throw new IllegalArgumentException("Field " + key + " must be an object");
      }
      return getIcon(obj.getAsJsonObject(key));
    }
    return null;
  }

  @Nullable
  public static Renderable getIcon(@Nullable JsonObject icon) {
    if (icon.has("texture")) {
      if (!icon.get("texture").isJsonPrimitive()) {
        throw new IllegalArgumentException("Field icon.texture must be a string");
      }
      return new Texture(new ResourceLocation(JsonUtils.getString(icon, "texture")), 16, 16, 0, 0, 16, 16);
    } else if (icon.has("item")) {
      if (!icon.get("item").isJsonPrimitive()) {
        throw new IllegalArgumentException("Field icon.item must be a string");
      }
      return new ItemRenderable(new ResourceLocation(JsonUtils.getString(icon, "item")));
    } else {
      return null;
    }
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

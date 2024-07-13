package org.infernalstudios.questlog.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.util.texture.ItemRenderable;
import org.infernalstudios.questlog.util.texture.Renderable;
import org.infernalstudios.questlog.util.texture.Texture;

import javax.annotation.Nullable;

public class JsonUtils {
  public static String getOrDefault(JsonObject json, String key, String defaultValue) {
    if (json.has(key)) {
      return json.get(key).getAsString();
    }
    return defaultValue;
  }

  public static int getOrDefault(JsonObject json, String key, int defaultValue) {
    if (json.has(key)) {
      return json.get(key).getAsInt();
    }
    return defaultValue;
  }

  public static boolean getOrDefault(JsonObject json, String key, boolean defaultValue) {
    if (json.has(key)) {
      return json.get(key).getAsBoolean();
    }
    return defaultValue;
  }

  public static double getOrDefault(JsonObject json, String key, double defaultValue) {
    if (json.has(key)) {
      return json.get(key).getAsDouble();
    }
    return defaultValue;
  }

  public static float getOrDefault(JsonObject json, String key, float defaultValue) {
    if (json.has(key)) {
      return json.get(key).getAsFloat();
    }
    return defaultValue;
  }

  public static JsonObject getOrDefault(JsonObject json, String key, JsonObject defaultValue) {
    if (json.has(key)) {
      return json.getAsJsonObject(key);
    }
    return defaultValue;
  }

  @Nullable
  public static Renderable getIcon(JsonObject obj, String key) {
    if (obj.has(key)) {
      return getIcon(obj.getAsJsonObject(key));
    }
    return null;
  }

  @Nullable
  public static Renderable getIcon(@Nullable JsonObject icon) {
    if (icon.has("texture")) {
      return new Texture(new ResourceLocation(icon.get("texture").getAsString()), 16, 16, 0, 0, 16, 16);
    } else if (icon.has("item")) {
      return new ItemRenderable(new ResourceLocation(icon.get("item").getAsString()));
    } else {
      return null;
    }
  }
}

package org.infernalstudios.questlog.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Util;

public class DefinitionUtil {

  private static final Map<ResourceLocation, JsonObject> QUEST_DEFINITION_CACHE = new Object2ObjectOpenHashMap<>();

  public static void getAndCacheAllQuests(ResourceManager manager) throws IOException {
    List<Resource> allQuestFiles = manager.getResourceStack(new ResourceLocation("questlog", "quests.json"));

    if (allQuestFiles.isEmpty()) {
      Questlog.LOGGER.error("No quest definition file found!");
      Questlog.LOGGER.error("Not crashing simply because it can happen.");
    }

    List<ResourceLocation> quests = new ArrayList<>();

    for (Resource res : allQuestFiles) {
      JsonObject json;

      try {
        json = Util.getJsonResource(res);
      } catch (MalformedJsonException e) {
        throw new MalformedJsonException("Malformed JSON in the quests.json file in datapack " + res.sourcePackId(), e);
      }

      JsonArray locArray = json.getAsJsonArray("quests");

      for (int i = 0; i < locArray.size(); i++) {
        quests.add(new ResourceLocation(locArray.get(i).getAsString()));
      }
    }

    QUEST_DEFINITION_CACHE.clear();

    if (quests.isEmpty()) {
      Questlog.LOGGER.warn("No quests found!");
    }

    for (ResourceLocation quest : quests) {
      try {
        QUEST_DEFINITION_CACHE.put(quest, Util.getJsonResource(manager, quest));
      } catch (MalformedJsonException e) {
        throw new MalformedJsonException("Malformed JSON in quest definition: " + quest.toString(), e);
      }
    }
  }

  public static List<ResourceLocation> getCachedKeys() {
    return new ArrayList<>(QUEST_DEFINITION_CACHE.keySet());
  }

  public static JsonObject getCached(ResourceLocation path) {
    if (!QUEST_DEFINITION_CACHE.containsKey(path)) {
      throw new NullPointerException("Quest not found: " + path);
    }

    return QUEST_DEFINITION_CACHE.get(path);
  }
}

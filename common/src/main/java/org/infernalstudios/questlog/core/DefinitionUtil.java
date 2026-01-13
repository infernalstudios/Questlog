package org.infernalstudios.questlog.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefinitionUtil {
    private static final Map<ResourceLocation, JsonObject> QUEST_DEFINITION_CACHE = new Object2ObjectOpenHashMap<>();

    public static List<ResourceLocation> getCachedKeys() {
        return new ArrayList<>(QUEST_DEFINITION_CACHE.keySet());
    }

    public static JsonObject getCached(ResourceLocation path) {
        if (!QUEST_DEFINITION_CACHE.containsKey(path)) {
            throw new NullPointerException("Quest not found: " + path);
        }

        return QUEST_DEFINITION_CACHE.get(path);
    }

    public static class QuestDefinitionReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
        @Override
        protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, JsonElement> prepared = new HashMap<>();

            List<Resource> allQuestFiles = manager.getResourceStack(ResourceLocation.fromNamespaceAndPath("questlog", "quests.json"));

            if (allQuestFiles.isEmpty()) {
                Questlog.LOGGER.warn("No quests.json file found!");
                return Map.of();
            }

            List<ResourceLocation> quests = new ArrayList<>();

            for (Resource res : allQuestFiles) {
                JsonObject json;

                try {
                    json = Util.getJsonResource(res);
                } catch (MalformedJsonException e) {
                    Questlog.LOGGER.error("Malformed JSON in quests.json in datapack {}", res.sourcePackId(), e);
                    continue;
                } catch (IOException e) {
                    Questlog.LOGGER.error("Error reading quests.json in datapack {}", res.sourcePackId(), e);
                    continue;
                }

                JsonArray locArray = json.getAsJsonArray("quests");

                for (int i = 0; i < locArray.size(); i++) {
                    ResourceLocation loc = ResourceLocation.parse(locArray.get(i).getAsString());
                    if (quests.contains(loc)) {
                        Questlog.LOGGER.warn("Duplicate quest in quests.json found: {}, skipping", loc);
                    } else {
                        quests.add(loc);
                    }
                }
            }

            if (quests.isEmpty()) {
                Questlog.LOGGER.warn("No quests found!");
                return Map.of();
            }

            for (ResourceLocation quest : quests) {
                try {
                    JsonObject json = Util.getJsonResource(manager, quest);
                    prepared.put(quest, json);
                } catch (MalformedJsonException e) {
                    Questlog.LOGGER.error("Malformed JSON in quest file {}", quest, e);
                } catch (IOException e) {
                    Questlog.LOGGER.error("Error reading quest file {}", quest, e);
                }
            }

            return prepared;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> questDefinitions, ResourceManager manager, ProfilerFiller profiler) {
            QUEST_DEFINITION_CACHE.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : questDefinitions.entrySet()) {
                try {
                    QUEST_DEFINITION_CACHE.put(entry.getKey(), entry.getValue().getAsJsonObject());
                } catch (JsonParseException | IllegalStateException e) {
                    Questlog.LOGGER.error("Error parsing quest definition for {}", entry.getKey(), e);
                }
            }
        }
    }
}

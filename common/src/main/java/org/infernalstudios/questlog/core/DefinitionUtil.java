package org.infernalstudios.questlog.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Util;
import org.jetbrains.annotations.NotNull;

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
        protected @NotNull Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, @NotNull ProfilerFiller profiler) {
            Map<ResourceLocation, JsonElement> prepared = new HashMap<>();

            Map<ResourceLocation, Resource> resources = manager.listResources("quests", loc -> loc.getPath().endsWith(".json"));

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                ResourceLocation fullLoc = entry.getKey();
                String path = fullLoc.getPath();

                String questName = path.substring("quests/".length(), path.length() - ".json".length());
                ResourceLocation questId = ResourceLocation.fromNamespaceAndPath(fullLoc.getNamespace(), questName);

                try {
                    JsonObject json = Util.getJsonResource(entry.getValue());
                    prepared.put(questId, json);
                } catch (Exception e) {
                    Questlog.LOGGER.error("=====================================================");
                    Questlog.LOGGER.error(" CRITICAL ERROR: Could not parse quest file: {}", fullLoc);
                    Questlog.LOGGER.error(" Reason: {}", e.getMessage());
                    Questlog.LOGGER.error(" This quest will be skipped!");
                    Questlog.LOGGER.error("=====================================================");
                }
            }

            if (prepared.isEmpty()) {
                Questlog.LOGGER.warn("No quests found in any datapacks!");
            }

            return prepared;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> questDefinitions, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
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
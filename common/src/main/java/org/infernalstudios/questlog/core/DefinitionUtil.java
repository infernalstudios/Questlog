package org.infernalstudios.questlog.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.platform.Services;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefinitionUtil {
    private static final Map<ResourceLocation, JsonObject> QUEST_DEFINITION_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<ResourceLocation> getCachedKeys() {
        return new ArrayList<>(QUEST_DEFINITION_CACHE.keySet());
    }

    public static JsonObject getCached(ResourceLocation path) {
        if (!QUEST_DEFINITION_CACHE.containsKey(path)) {
            throw new NullPointerException("Quest not found: " + path);
        }

        return QUEST_DEFINITION_CACHE.get(path);
    }

    public static void loadFromConfig() {
        QUEST_DEFINITION_CACHE.clear();
        Path questDir = Services.PLATFORM.getConfigDirectory().resolve("questlog").resolve("quests");

        if (!Files.exists(questDir)) {
            try {
                Files.createDirectories(questDir);
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to create quests directory: {}", questDir, e);
                return;
            }
        }

        try (Stream<Path> paths = Files.walk(questDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(DefinitionUtil::loadQuestFile);
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to read quests from directory: {}", questDir, e);
        }

        if (QUEST_DEFINITION_CACHE.isEmpty()) {
            Questlog.LOGGER.warn("No quests found in config/questlog/quests!");
        } else {
            Questlog.LOGGER.info("Loaded {} quests from config.", QUEST_DEFINITION_CACHE.size());
        }
    }

    private static void loadQuestFile(Path path) {
        try (FileReader reader = new FileReader(path.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            Path questDir = Services.PLATFORM.getConfigDirectory().resolve("questlog").resolve("quests");
            Path relative = questDir.relativize(path);
            String resourcePath = relative.toString().replace(File.separatorChar, '/').replace(".json", "");

            ResourceLocation questId = ResourceLocation.fromNamespaceAndPath(Questlog.MODID, resourcePath);

            QUEST_DEFINITION_CACHE.put(questId, json);
        } catch (Exception e) {
            Questlog.LOGGER.error("=====================================================");
            Questlog.LOGGER.error(" CRITICAL ERROR: Could not parse quest file: {}", path);
            Questlog.LOGGER.error(" Reason: {}", e.getMessage());
            Questlog.LOGGER.error(" This quest will be skipped!");
            Questlog.LOGGER.error("=====================================================");
        }
    }
}
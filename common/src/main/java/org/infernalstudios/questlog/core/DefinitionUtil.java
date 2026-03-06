package org.infernalstudios.questlog.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefinitionUtil {
    private static final Map<ResourceLocation, JsonObject> QUEST_DEFINITION_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, JsonObject> CHAPTER_DEFINITION_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static List<ResourceLocation> getCachedQuestKeys() {
        return new ArrayList<>(QUEST_DEFINITION_CACHE.keySet());
    }

    public static JsonObject getCachedQuest(ResourceLocation path) {
        if (!QUEST_DEFINITION_CACHE.containsKey(path)) {
            throw new NullPointerException("Quest not found: " + path);
        }
        return QUEST_DEFINITION_CACHE.get(path);
    }

    public static List<ResourceLocation> getCachedChapterKeys() {
        List<ResourceLocation> keys = new ArrayList<>(CHAPTER_DEFINITION_CACHE.keySet());
        keys.sort((a, b) -> {
            boolean aMain = a.getNamespace().equals(Questlog.MODID) && a.getPath().equals("main");
            boolean bMain = b.getNamespace().equals(Questlog.MODID) && b.getPath().equals("main");
            if (aMain && !bMain) return -1;
            if (!aMain && bMain) return 1;

            int orderA = JsonUtils.getOrDefault(CHAPTER_DEFINITION_CACHE.get(a), "order", 0);
            int orderB = JsonUtils.getOrDefault(CHAPTER_DEFINITION_CACHE.get(b), "order", 0);

            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }
            return a.compareTo(b);
        });
        return keys;
    }

    public static synchronized JsonObject getCachedChapter(ResourceLocation path) {
        return CHAPTER_DEFINITION_CACHE.get(path);
    }

    public static void putCachedChapter(ResourceLocation path, JsonObject definition) {
        CHAPTER_DEFINITION_CACHE.put(path, definition);
    }

    public static synchronized void loadFromConfig() {
        QUEST_DEFINITION_CACHE.clear();
        CHAPTER_DEFINITION_CACHE.clear();

        Path configDir = Services.PLATFORM.getConfigDirectory().resolve("questlog");
        Path questDir = configDir.resolve("quests");
        Path chapterDir = configDir.resolve("chapters");

        createDirIfNotExists(questDir);
        createDirIfNotExists(chapterDir);

        Path defaultMainChapter = chapterDir.resolve("main.json");
        if (!Files.exists(defaultMainChapter)) {
            try {
                JsonObject mainChapter = new JsonObject();
                JsonObject iconObj = new JsonObject();
                iconObj.addProperty("item", "minecraft:knowledge_book");
                mainChapter.add("icon", iconObj);
                mainChapter.addProperty("default_chapter", true);
                mainChapter.addProperty("hidden", false);

                Files.writeString(defaultMainChapter, GSON.toJson(mainChapter));
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to create default main.json chapter", e);
            }
        }

        loadFiles(questDir, QUEST_DEFINITION_CACHE);
        loadFiles(chapterDir, CHAPTER_DEFINITION_CACHE);

        Questlog.LOGGER.info("Loaded {} quests and {} chapters from config.", QUEST_DEFINITION_CACHE.size(), CHAPTER_DEFINITION_CACHE.size());
    }

    private static void createDirIfNotExists(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to create directory: {}", dir, e);
            }
        }
    }

    private static void loadFiles(Path dir, Map<ResourceLocation, JsonObject> cache) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                            JsonObject json = GSON.fromJson(reader, JsonObject.class);
                            Path relative = dir.relativize(path);
                            String resourcePath = relative.toString().replace(File.separatorChar, '/').replace(".json", "");
                            ResourceLocation id = new ResourceLocation(Questlog.MODID, resourcePath);
                            cache.put(id, json);
                        } catch (Exception e) {
                            Questlog.LOGGER.error("Failed to parse file: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to read files from directory: {}", dir, e);
        }
    }
}
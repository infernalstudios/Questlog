package org.infernalstudios.questlog.util;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.platform.Services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class QuestlogMigrator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean showDatapackWarning = false;

    public static void attemptMigration(MinecraftServer server) {
        Path configDir = Services.PLATFORM.getConfigDirectory().resolve("questlog");
        Path questsDir = configDir.resolve("quests");

        try {
            if (!Files.exists(questsDir)) {
                Files.createDirectories(questsDir);
            }

            // Check if the new quests directory already has files
            try (Stream<Path> stream = Files.list(questsDir)) {
                if (stream.findAny().isPresent()) {
                    return;
                }
            }

            // Look for old quests in data/questlog/quests/*.json
            ResourceManager resourceManager = server.getResourceManager();
            Map<ResourceLocation, Resource> oldQuests = resourceManager.listResources("quests",
                    loc -> loc.getNamespace().equals(Questlog.MODID) && loc.getPath().endsWith(".json"));

            if (oldQuests.isEmpty()) {
                return;
            }

            Questlog.LOGGER.info("Starting automated migration of old Questlog quests...");
            int migratedCount = 0;

            for (Map.Entry<ResourceLocation, Resource> entry : oldQuests.entrySet()) {
                ResourceLocation id = entry.getKey();

                // Exclude the old registry file if it exists in this folder by accident
                if (id.getPath().equals("quests.json") || id.getPath().equals("quests/quests.json")) {
                    continue;
                }

                try {
                    JsonObject oldQuest = Util.getJsonResource(entry.getValue());
                    JsonObject newQuest = convertQuestFormat(oldQuest);

                    // Path starts with "quests/", strip it to get relative filename
                    String relativePath = id.getPath().replaceFirst("^quests/", "");
                    Path targetPath = questsDir.resolve(relativePath);

                    if (!Files.exists(targetPath.getParent())) {
                        Files.createDirectories(targetPath.getParent());
                    }

                    Files.writeString(targetPath, GSON.toJson(newQuest));
                    migratedCount++;
                } catch (Exception e) {
                    Questlog.LOGGER.error("Failed to migrate old quest: {}", id, e);
                }
            }

            if (migratedCount > 0) {
                Questlog.LOGGER.info("Successfully migrated {} quests to the new config format.", migratedCount);
                showDatapackWarning = true;
            }

        } catch (Exception e) {
            Questlog.LOGGER.error("An error occurred during Questlog migration", e);
        }
    }

    private static JsonObject convertQuestFormat(JsonObject oldQuest) {
        JsonObject newQuest = new JsonObject();

        if (oldQuest.has("triggers") && oldQuest.get("triggers").isJsonArray()) {
            newQuest.add("requirements", convertObjectives(oldQuest.getAsJsonArray("triggers")));
        }

        if (oldQuest.has("objectives") && oldQuest.get("objectives").isJsonArray()) {
            newQuest.add("objectives", convertObjectives(oldQuest.getAsJsonArray("objectives")));
        }

        if (oldQuest.has("rewards") && oldQuest.get("rewards").isJsonArray()) {
            newQuest.add("rewards", oldQuest.getAsJsonArray("rewards"));
        }

        if (oldQuest.has("display") && oldQuest.get("display").isJsonObject()) {
            JsonObject display = oldQuest.getAsJsonObject("display");

            // Migrate top-level display fields
            if (display.has("title")) newQuest.add("title", display.get("title"));
            if (display.has("description")) newQuest.add("description", display.get("description"));
            if (display.has("icon")) newQuest.add("icon", display.get("icon"));

            // Flatten style.background.texture -> background_texture
            if (display.has("style") && display.get("style").isJsonObject()) {
                JsonObject style = display.getAsJsonObject("style");
                if (style.has("background") && style.get("background").isJsonObject()) {
                    JsonObject bg = style.getAsJsonObject("background");
                    if (bg.has("texture")) {
                        newQuest.add("background_texture", bg.get("texture"));
                    }
                }
            }

            // Flatten notifications -> toast_on_unlock / toast_on_complete
            if (display.has("notification") && display.get("notification").isJsonObject()) {
                JsonObject notification = display.getAsJsonObject("notification");
                if (notification.has("toastOnComplete")) {
                    newQuest.add("toast_on_complete", notification.get("toastOnComplete"));
                }
                if (notification.has("toastOnTrigger")) {
                    newQuest.add("toast_on_unlock", notification.get("toastOnTrigger"));
                }
            }

            // Flatten sound -> triggered_sound / completed_sound
            if (display.has("sound") && display.get("sound").isJsonObject()) {
                JsonObject sound = display.getAsJsonObject("sound");
                if (sound.has("triggered")) newQuest.add("triggered_sound", sound.get("triggered"));
                if (sound.has("completed")) newQuest.add("completed_sound", sound.get("completed"));
            }
        }

        return newQuest;
    }

    private static JsonArray convertObjectives(JsonArray oldObjectives) {
        JsonArray newObjectives = new JsonArray();

        for (JsonElement element : oldObjectives) {
            if (element.isJsonObject()) {
                JsonObject oldObj = element.getAsJsonObject();
                JsonObject newObj = oldObj.deepCopy();

                // Rename "total" -> "required_amount"
                if (newObj.has("total")) {
                    newObj.add("required_amount", newObj.get("total"));
                    newObj.remove("total");
                }

                // Rename "item_pickup" -> "item_obtain"
                if (newObj.has("type")) {
                    String type = newObj.get("type").getAsString();
                    if (type.equals("questlog:item_pickup") || type.equals("item_pickup")) {
                        newObj.addProperty("type", "questlog:item_obtain");
                    }
                }

                // Flatten display.name -> name
                if (newObj.has("display") && newObj.get("display").isJsonObject()) {
                    JsonObject display = newObj.getAsJsonObject("display");
                    if (display.has("name")) {
                        newObj.add("name", display.get("name"));
                    }
                    newObj.remove("display");
                }

                newObjectives.add(newObj);
            }
        }

        return newObjectives;
    }
}
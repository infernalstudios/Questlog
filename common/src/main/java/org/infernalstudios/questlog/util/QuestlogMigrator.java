package org.infernalstudios.questlog.util;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.platform.Services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class QuestlogMigrator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static boolean showDatapackWarning = false;

    public static void attemptMigration(MinecraftServer server) {
        attemptDatapackMigration(server);
        attemptPlayerDataMigration(server);
    }

    private static void attemptDatapackMigration(MinecraftServer server) {
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

    private static void attemptPlayerDataMigration(MinecraftServer server) {
        Path playerDataDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR);
        if (!Files.exists(playerDataDir)) return;

        try (Stream<Path> stream = Files.list(playerDataDir)) {
            stream.filter(path -> path.toString().endsWith(".questlog.dat")).forEach(path -> {
                try {
                    File file = path.toFile();
                    CompoundTag oldData = NbtIo.readCompressed(file);
                    CompoundTag newData = new CompoundTag();
                    boolean migratedAny = false;

                    for (String key : oldData.getAllKeys()) {
                        String newKey = key;
                        boolean migratedThisQuest = false;

                        // Migrate key names (remove "quests/" folder prefix from the path)
                        if (key.startsWith(Questlog.MODID + ":quests/")) {
                            newKey = key.replaceFirst(":quests/", ":");
                            migratedThisQuest = true;
                        }

                        CompoundTag oldQuestData = oldData.getCompound(key);
                        CompoundTag newQuestData = new CompoundTag();

                        if (oldQuestData.contains("triggered")) {
                            newQuestData.putBoolean("triggered", oldQuestData.getBoolean("triggered"));
                        }
                        if (oldQuestData.contains("completed")) {
                            newQuestData.putBoolean("completed", oldQuestData.getBoolean("completed"));
                        }

                        // Migrate "triggers" / "requirements" -> "prerequisites"
                        if (oldQuestData.contains("triggers", Tag.TAG_LIST)) {
                            newQuestData.put("prerequisites", oldQuestData.getList("triggers", Tag.TAG_COMPOUND).copy());
                            migratedThisQuest = true;
                        } else if (oldQuestData.contains("prerequisites", Tag.TAG_LIST)) {
                            newQuestData.put("prerequisites", oldQuestData.getList("prerequisites", Tag.TAG_COMPOUND).copy());
                        } else if (oldQuestData.contains("requirements", Tag.TAG_LIST)) {
                            newQuestData.put("prerequisites", oldQuestData.getList("requirements", Tag.TAG_COMPOUND).copy());
                            migratedThisQuest = true;
                        }

                        // Copy remaining unchanged lists
                        if (oldQuestData.contains("objectives", Tag.TAG_LIST)) {
                            newQuestData.put("objectives", oldQuestData.getList("objectives", Tag.TAG_COMPOUND).copy());
                        }
                        if (oldQuestData.contains("rewards", Tag.TAG_LIST)) {
                            newQuestData.put("rewards", oldQuestData.getList("rewards", Tag.TAG_COMPOUND).copy());
                        }

                        if (migratedThisQuest) {
                            migratedAny = true;
                        }

                        newData.put(newKey, newQuestData);
                    }

                    if (migratedAny) {
                        Questlog.LOGGER.info("Successfully migrated old Questlog player data for {}", file.getName());
                        NbtIo.writeCompressed(newData, file);
                    }
                } catch (Exception e) {
                    Questlog.LOGGER.error("Failed to migrate player data: {}", path.getFileName(), e);
                }
            });
        } catch (Exception e) {
            Questlog.LOGGER.error("An error occurred during Questlog player data migration", e);
        }
    }

    private static JsonObject convertQuestFormat(JsonObject oldQuest) {
        JsonObject newQuest = new JsonObject();

        if (oldQuest.has("prerequisites") && oldQuest.get("prerequisites").isJsonArray()) {
            newQuest.add("prerequisites", convertObjectives(oldQuest.getAsJsonArray("prerequisites")));
        } else if (oldQuest.has("requirements") && oldQuest.get("requirements").isJsonArray()) {
            newQuest.add("prerequisites", convertObjectives(oldQuest.getAsJsonArray("requirements")));
        } else if (oldQuest.has("triggers") && oldQuest.get("triggers").isJsonArray()) {
            newQuest.add("prerequisites", convertObjectives(oldQuest.getAsJsonArray("triggers")));
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

                    if (type.equals("questlog:quest_complete") || type.equals("quest_complete")) {
                        if (newObj.has("quest")) {
                            String oldQuestId = newObj.get("quest").getAsString();
                            if (oldQuestId.contains(":quests/")) {
                                newObj.addProperty("quest", oldQuestId.replaceFirst(":quests/", ":"));
                            }
                        }
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
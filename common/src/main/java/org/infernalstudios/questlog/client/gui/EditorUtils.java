package org.infernalstudios.questlog.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.network.packet.ChapterEditRemovePacket;
import org.infernalstudios.questlog.network.packet.QuestEditRemovePacket;
import org.infernalstudios.questlog.network.packet.QuestEditSavePacket;
import org.infernalstudios.questlog.platform.Services;
import org.infernalstudios.questlog.util.Util;

import java.util.*;

public class EditorUtils {
    public static JsonObject copiedQuestJson = null;

    public static ResourceLocation getUniqueQuestId(ResourceLocation originalId) {
        String namespace = originalId.getNamespace();

        String baseName = originalId.getPath();
        if (baseName.endsWith("_copy")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        } else {
            baseName = baseName.replaceAll("_copy_\\d+$", "");
        }

        String candidateBase = baseName + "_copy";
        ResourceLocation newId = new ResourceLocation(namespace, candidateBase);
        int counter = 1;
        while (DefinitionUtil.getCachedQuestKeys().contains(newId)) {
            newId = new ResourceLocation(namespace, candidateBase + "_" + counter);
            counter++;
        }
        return newId;
    }

    public static void copyQuestToClipboard(ResourceLocation questId) {
        try {
            JsonObject json = DefinitionUtil.getCachedQuest(questId);
            copiedQuestJson = JsonParser.parseString(json.toString()).getAsJsonObject();
            Minecraft mc = Minecraft.getInstance();
            mc.keyboardHandler.setClipboard(json.toString());
        } catch (Exception e) {
            Questlog.LOGGER.error("Failed to copy quest to clipboard", e);
        }
    }

    public static void duplicateQuest(ResourceLocation questId) {
        try {
            JsonObject originalJson = DefinitionUtil.getCachedQuest(questId);
            JsonObject clonedJson = JsonParser.parseString(originalJson.toString()).getAsJsonObject();

            ResourceLocation newId = getUniqueQuestId(questId);

            String currentTitle = clonedJson.has("title") ? clonedJson.get("title").getAsString() : "New Quest";
            clonedJson.addProperty("title", currentTitle + " (Copy)");

            Services.PLATFORM.sendPacketToServer(new QuestEditSavePacket(newId, clonedJson.toString()));
        } catch (Exception e) {
            Questlog.LOGGER.error("Failed to duplicate quest", e);
        }
    }

    public static void pasteQuest(ResourceLocation chapterId) {
        JsonObject jsonToPaste = copiedQuestJson;
        Minecraft mc = Minecraft.getInstance();
        if (jsonToPaste == null) {
            try {
                String clipboard = mc.keyboardHandler.getClipboard();
                jsonToPaste = JsonParser.parseString(clipboard).getAsJsonObject();
            } catch (Exception ignored) {
            }
        }
        if (jsonToPaste == null) return;

        try {
            JsonObject clonedJson = JsonParser.parseString(jsonToPaste.toString()).getAsJsonObject();

            String baseIdPath = "pasted_quest";
            if (clonedJson.has("title")) {
                baseIdPath = clonedJson.get("title").getAsString().toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9/._-]", "_");
                if (baseIdPath.isEmpty()) {
                    baseIdPath = "pasted_quest";
                }
            }
            ResourceLocation baseId = new ResourceLocation(Questlog.MODID, baseIdPath);
            ResourceLocation newId = getUniqueQuestId(baseId);

            clonedJson.addProperty("chapter", chapterId.toString());

            String currentTitle = clonedJson.has("title") ? clonedJson.get("title").getAsString() : "New Quest";
            clonedJson.addProperty("title", currentTitle + " (Copy)");

            Services.PLATFORM.sendPacketToServer(new QuestEditSavePacket(newId, clonedJson.toString()));
        } catch (Exception e) {
            Questlog.LOGGER.error("Failed to paste quest", e);
        }
    }

    public static boolean hasCopiedQuest() {
        if (copiedQuestJson != null) return true;
        try {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
            JsonObject obj = JsonParser.parseString(clipboard).getAsJsonObject();
            return obj.has("title") || obj.has("chapter") || obj.has("objectives");
        } catch (Exception e) {
            return false;
        }
    }

    public static void deleteQuest(ResourceLocation questId) {
        Services.PLATFORM.sendPacketToServer(new QuestEditRemovePacket(questId));
    }

    public static void deleteChapter(ResourceLocation chapterId) {
        String chapPath = chapterId.getPath();
        if (!chapPath.equals("main")) {
            for (ResourceLocation qKey : DefinitionUtil.getCachedQuestKeys()) {
                try {
                    JsonObject qJson = DefinitionUtil.getCachedQuest(qKey);
                    String qChap = qJson.has("chapter") ? qJson.get("chapter").getAsString() : "main";
                    if (qChap.equals(chapPath)) {
                        qJson.addProperty("chapter", "main");
                        Services.PLATFORM.sendPacketToServer(new QuestEditSavePacket(qKey, qJson.toString()));
                    }
                } catch (Exception ignored) {
                }
            }
            Services.PLATFORM.sendPacketToServer(new ChapterEditRemovePacket(chapterId));
        }
    }

    public static List<EditorPreset> getPresets(String subfolder) {
        List<EditorPreset> presets = new ArrayList<>();
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            String prefix = "presets/" + subfolder;
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                    prefix,
                    loc -> loc.getNamespace().equals(Questlog.MODID) && loc.getPath().endsWith(".json")
            );

            Map<ResourceLocation, Resource> sortedResources = new TreeMap<>(resources);
            for (Map.Entry<ResourceLocation, Resource> entry : sortedResources.entrySet()) {
                ResourceLocation loc = entry.getKey();
                try {
                    JsonObject json = Util.getJsonResource(entry.getValue());
                    String filename = loc.getPath();
                    if (filename.startsWith(prefix + "/")) {
                        filename = filename.substring((prefix + "/").length());
                    }
                    if (filename.endsWith(".json")) {
                        filename = filename.substring(0, filename.length() - 5);
                    }
                    String title = json.has("title") ? json.get("title").getAsString() : filename;
                    String description = json.has("description") ? json.get("description").getAsString() : "";
                    presets.add(new EditorPreset(filename, title, description, json));
                } catch (Exception e) {
                    Questlog.LOGGER.error("Failed to load preset: {}", loc, e);
                }
            }
        } catch (Exception e) {
            Questlog.LOGGER.error("Failed to list presets in {}", subfolder, e);
        }
        return presets;
    }

    public record EditorPreset(String filename, String title, String description, JsonObject json) {
    }
}

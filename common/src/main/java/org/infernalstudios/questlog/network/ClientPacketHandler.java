package org.infernalstudios.questlog.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogEvents;
import org.infernalstudios.questlog.client.gui.screen.ChapterEditorScreen;
import org.infernalstudios.questlog.client.gui.screen.QuestEditorScreen;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.packet.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientPacketHandler {
    private static final Gson GSON = new GsonBuilder().create();
    private static final List<QuestDefinitionPacket> DEFERRED_DEFS = new CopyOnWriteArrayList<>();
    private static QuestSyncPacket DEFERRED_SYNC_PACKET = null;

    public static void handle(QuestCompletedPacket packet, IPacketContext ctx) {
        QuestManager manager = QuestlogClient.getLocal();
        QuestlogEvents.onQuestCompleted(new QuestEvent.Completed(manager.player, manager.getQuest(packet.id()), false));
    }

    public static void handle(QuestDataPacket packet, IPacketContext ctx) {
        try {
            QuestManager manager = QuestlogClient.getLocal();
            Quest quest = manager.getQuest(packet.id());
            if (quest == null) {
                throw new IllegalStateException("Quest is null, likely definition not loaded yet");
            }
            quest.deserialize(packet.data());
        } catch (Throwable e) {
            Questlog.LOGGER.error("Failed to handle QuestDataPacket", e);
        }
    }

    public static void handle(QuestDefinitionPacket packet, IPacketContext ctx) {
        if (Minecraft.getInstance().player == null) {
            DEFERRED_DEFS.add(packet);
            return;
        }
        try {
            QuestManager manager = QuestlogClient.getLocal();
            Quest existing = manager.getQuest(packet.id());
            CompoundTag savedData = existing != null ? existing.serialize() : null;
            Quest quest = Quest.create(Objects.requireNonNull(GSON.fromJson(packet.getJsonString(), JsonObject.class)), packet.id(), manager);
            if (savedData != null) {
                quest.deserialize(savedData);
            }
            manager.addQuest(quest);
        } catch (Throwable e) {
            Questlog.LOGGER.error("Failed to handle QuestDefinitionPacket", e);
        }
    }

    public static void handleDeferredDefinitions() {
        for (QuestDefinitionPacket packet : DEFERRED_DEFS) {
            handle(packet, null);
        }
        DEFERRED_DEFS.clear();
    }

    public static void handle(QuestRemovePacket packet, IPacketContext ctx) {
        Questlog.LOGGER.trace("Received remove packet for quest {}", packet.id().toString());
        QuestManager manager = QuestlogClient.getLocal();
        manager.removeQuest(packet.id());
    }

    public static void handle(QuestTriggeredPacket packet, IPacketContext ctx) {
        QuestManager manager = QuestlogClient.getLocal();
        QuestlogEvents.onQuestTriggered(new QuestEvent.Triggered(manager.player, manager.getQuest(packet.id()), false));
    }

    public static void handle(QuestOpenPacket packet, IPacketContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String target = packet.target();
        if (target != null && !target.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(target);
            if (id != null) {
                Quest quest = QuestlogClient.getLocal().getQuest(id);
                if (quest != null) {
                    mc.setScreen(new QuestDetails(mc.screen, quest));
                    return;
                }
            }
        }
        mc.setScreen(new QuestlogScreen(mc.screen));
    }

    public static void handle(QuestEditModePacket packet, IPacketContext ctx) {
        QuestlogClient.isEditModeActive = packet.enabled();
        Questlog.LOGGER.info("Questlog Edit Mode has been set to: {}", packet.enabled());
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof QuestlogScreen questlogScreen) {
            questlogScreen.init(mc, questlogScreen.width, questlogScreen.height);
        }
    }

    public static void handle(QuestSyncPacket packet, IPacketContext ctx) {
        if (Minecraft.getInstance().player == null) {
            DEFERRED_SYNC_PACKET = packet;
            return;
        }
        processSync(packet);
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof QuestlogScreen questlogScreen) {
            questlogScreen.init(mc, questlogScreen.width, questlogScreen.height);
        } else if (mc.screen instanceof QuestDetails detailsScreen) {
            Quest updatedQuest = QuestlogClient.getLocal().getQuest(detailsScreen.quest.getId());
            if (updatedQuest != null) {
                mc.setScreen(new QuestDetails(detailsScreen.getPreviousScreen(), updatedQuest));
            } else {
                mc.setScreen(new QuestlogScreen(detailsScreen.getPreviousScreen()));
            }
        } else if (mc.screen instanceof ChapterEditorScreen chapterEditorScreen) {
            chapterEditorScreen.saveTemporaryState();
            chapterEditorScreen.refreshScreen();
        } else if (mc.screen instanceof QuestEditorScreen questEditorScreen) {
            questEditorScreen.saveTemporaryState();
            questEditorScreen.refreshScreen();
        }
    }

    public static void handleDeferredSync() {
        if (DEFERRED_SYNC_PACKET != null) {
            processSync(DEFERRED_SYNC_PACKET);
            DEFERRED_SYNC_PACKET = null;
        }
    }

    private static void processSync(QuestSyncPacket packet) {
        Questlog.LOGGER.info("Received quest & chapter sync from server.");
        DefinitionUtil.clearClientCaches();
        for (Map.Entry<ResourceLocation, String> entry : packet.chapterDefinitions().entrySet()) {
            try {
                JsonObject def = GSON.fromJson(entry.getValue(), JsonObject.class);
                if (def != null) {
                    DefinitionUtil.putCachedChapter(entry.getKey(), def);
                }
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse synced chapter {}", entry.getKey(), e);
            }
        }
        QuestManager manager = QuestlogClient.getLocal();
        manager.clearQuests();
        for (Map.Entry<ResourceLocation, String> entry : packet.definitions().entrySet()) {
            try {
                JsonObject def = GSON.fromJson(entry.getValue(), JsonObject.class);
                if (def != null) {
                    DefinitionUtil.putCachedQuest(entry.getKey(), def);
                }
                Quest quest = Quest.create(def, entry.getKey(), manager);
                manager.addQuest(quest);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse synced quest {}", entry.getKey(), e);
            }
        }
        for (Map.Entry<ResourceLocation, CompoundTag> entry : packet.data().entrySet()) {
            Quest quest = manager.getQuest(entry.getKey());
            if (quest != null) {
                quest.deserialize(entry.getValue());
            }
        }
    }
}
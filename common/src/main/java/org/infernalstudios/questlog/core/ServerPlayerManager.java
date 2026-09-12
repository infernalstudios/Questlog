package org.infernalstudios.questlog.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.packet.QuestSyncPacket;
import org.infernalstudios.questlog.network.packet.QuestEditModePacket;
import org.infernalstudios.questlog.platform.Services;
import net.minecraft.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.advancements.AdvancementHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerPlayerManager {

    public static ServerPlayerManager INSTANCE = null;

    private final Map<UUID, QuestManager> questManagers = new HashMap<>();
    private final MinecraftServer server;

    public ServerPlayerManager(MinecraftServer server) {
        this.server = server;
        for (Player player : this.server.getPlayerList().getPlayers()) {
            this.addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        QuestManager questManager = new QuestManager(player);
        this.questManagers.put(player.getUUID(), questManager);
    }

    public QuestManager getManagerIfPresent(Player player) {
        if (!this.questManagers.containsKey(player.getUUID())) {
            return null;
        } else if (this.questManagers.get(player.getUUID()).player != player) {
            this.questManagers.get(player.getUUID()).player = player;
        }

        return this.questManagers.get(player.getUUID());
    }

    public QuestManager getManagerByPlayer(Player player) {
        if (!this.questManagers.containsKey(player.getUUID())) {
            this.addPlayer(player);
        } else if (this.questManagers.get(player.getUUID()).player != player) {
            this.questManagers.get(player.getUUID()).player = player;
        }

        return this.questManagers.get(player.getUUID());
    }

    /**
     * This method is used to save the data of all quest managers.
     * It iterates over all quest managers and calls the save method for each one.
     */
    public void save() {
        for (QuestManager questManager : this.questManagers.values()) {
            this.save(questManager);
        }
    }

    /**
     * This method is used to save the data of a specific quest manager.
     * It creates a new CompoundTag, serializes the data of all quests in the quest manager, and writes the data to a file.
     *
     * @param questManager The quest manager whose data will be saved.
     */
    public void save(QuestManager questManager) {
        if (!questManager.isLoaded()) {
            Questlog.LOGGER.warn("Skipping quest save for player {} because quest data has not been loaded yet", questManager.player.getGameProfile().getName());
            return;
        }

        Questlog.LOGGER.debug("Saving player data for {}", questManager.player.getGameProfile().getName());
        CompoundTag data = new CompoundTag();
        for (Quest quest : questManager.getAllQuests()) {
            data.put(quest.getId().toString(), quest.serialize());
        }

        data.putBoolean("edit_mode", questManager.isEditMode());

        Path tempFile = null;
        try {
            Path playerDataDir = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
            Files.createDirectories(playerDataDir);
            String uuid = questManager.player.getStringUUID();
            tempFile = Files.createTempFile(playerDataDir, uuid + "-", ".questlog.dat");
            NbtIo.writeCompressed(data, tempFile);
            Path targetFile = playerDataDir.resolve(uuid + ".questlog.dat");
            Path backupFile = playerDataDir.resolve(uuid + ".questlog.dat_old");
            Util.safeReplaceFile(targetFile, tempFile, backupFile);
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to save player data for {}", questManager.player.getGameProfile().getName(), e);
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * This method is used to load the data of all quest managers.
     * It iterates over all players and calls the load method for each player's quest manager.
     */
    public void load() {
        Questlog.LOGGER.trace("Loading all player data");
        for (Player player : this.server.getPlayerList().getPlayers()) {
            this.load(this.getManagerByPlayer(player));
        }
    }

    /**
     * This method is used to load the data of a specific quest manager.
     * It reads the data from a file, deserializes the data, and updates the quests in the quest manager.
     * If a quest does not exist in the data, it will be created.
     * After loading the data, it checks if the data should be saved (if a quest was created).
     *
     * @param questManager The quest manager whose data will be loaded.
     */
    public void load(QuestManager questManager) {
        Questlog.LOGGER.debug("Loading player data for {}", questManager.player.getGameProfile().getName());
        File playerDataFile = this.getPlayerDataFile(questManager.player);
        File backupDataFile = this.getBackupPlayerDataFile(questManager.player);

        CompoundTag data = null;
        if (playerDataFile.exists()) {
            try {
                data = NbtIo.readCompressed(playerDataFile.toPath(), NbtAccounter.unlimitedHeap());
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to load player data for {}, attempting backup", questManager.player.getGameProfile().getName(), e);
            }
        }

        if (data == null && backupDataFile.exists()) {
            try {
                data = NbtIo.readCompressed(backupDataFile.toPath(), NbtAccounter.unlimitedHeap());
                Questlog.LOGGER.info("Successfully loaded backup quest data for {}", questManager.player.getGameProfile().getName());
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to load backup player data for {}", questManager.player.getGameProfile().getName(), e);
            }
        }

        if (data == null) {
            data = new CompoundTag();
        }

        boolean shouldSave = false;
        questManager.reload();

        if (data.contains("edit_mode")) {
            questManager.setEditMode(data.getBoolean("edit_mode"));
        } else {
            questManager.setEditMode(false);
        }

        CompoundTag globalData = this.loadGlobalData();
        for (Quest quest : questManager.getAllQuests()) {
            if (data.contains(quest.getId().toString())) {
                CompoundTag questData = data.getCompound(quest.getId().toString());
                quest.deserialize(questData);
            } else {
                shouldSave = true;
            }

            if (quest.isGlobal() && globalData.contains(quest.getId().toString())) {
                quest.deserialize(globalData.getCompound(quest.getId().toString()));
            }
        }

        questManager.setLoaded(true);

        if (shouldSave) {
            this.save(questManager);
        }

        this.syncPlayer(questManager);
    }

    private boolean isSyncingGlobal = false;

    private File getGlobalDataFile() {
        Path playerDataPath = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        return new File(playerDataPath.toFile(), "global_quests.questlog.dat");
    }

    private CompoundTag loadGlobalData() {
        File file = getGlobalDataFile();
        File backupFile = new File(file.getParentFile(), "global_quests.questlog.dat_old");
        if (file.exists()) {
            try {
                return NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to load global quest data, attempting backup", e);
            }
        }
        if (backupFile.exists()) {
            try {
                return NbtIo.readCompressed(backupFile.toPath(), NbtAccounter.unlimitedHeap());
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to load backup global quest data", e);
            }
        }
        return new CompoundTag();
    }

    private void saveGlobalData(CompoundTag tag) {
        Path tempFile = null;
        try {
            Path playerDataDir = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
            Files.createDirectories(playerDataDir);
            tempFile = Files.createTempFile(playerDataDir, "global_quests-", ".questlog.dat");
            NbtIo.writeCompressed(tag, tempFile);
            Path targetFile = playerDataDir.resolve("global_quests.questlog.dat");
            Path backupFile = playerDataDir.resolve("global_quests.questlog.dat_old");
            Util.safeReplaceFile(targetFile, tempFile, backupFile);
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to save global quest data", e);
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void onGlobalQuestUpdated(Quest sourceQuest) {
        if (isSyncingGlobal) return;
        isSyncingGlobal = true;
        try {
            ResourceLocation id = sourceQuest.getId();
            CompoundTag serialized = sourceQuest.serialize();

            CompoundTag globalTag = loadGlobalData();
            globalTag.put(id.toString(), serialized);
            saveGlobalData(globalTag);

            for (QuestManager manager : this.questManagers.values()) {
                Quest q = manager.getQuest(id);
                if (q != null) {
                    q.deserialize(serialized);
                    manager.sync(id);
                }
            }
        } finally {
            isSyncingGlobal = false;
        }
    }

    public void resetGlobalQuest(ResourceLocation id) {
        if (isSyncingGlobal) return;
        isSyncingGlobal = true;
        try {
            CompoundTag globalTag = loadGlobalData();
            for (QuestManager manager : this.questManagers.values()) {
                Quest q = manager.getQuest(id);
                if (q != null) {
                    q.resetProgress();
                    globalTag.put(id.toString(), q.serialize());
                }
            }
            saveGlobalData(globalTag);
            this.save();
        } finally {
            isSyncingGlobal = false;
        }
    }

    public void syncPlayer(QuestManager questManager) {
        if (questManager.player instanceof ServerPlayer serverPlayer) {
            Map<ResourceLocation, String> definitions = new HashMap<>();
            Map<ResourceLocation, String> chapterDefinitions = new HashMap<>();
            Map<ResourceLocation, CompoundTag> data = new HashMap<>();

            for (Quest quest : questManager.getAllQuests()) {
                definitions.put(quest.getId(), DefinitionUtil.getCachedQuest(quest.getId()).toString());
                data.put(quest.getId(), quest.serialize());
            }

            for (ResourceLocation chapterId : DefinitionUtil.getCachedChapterKeys()) {
                var chapterJson = DefinitionUtil.getCachedChapter(chapterId);
                if (chapterJson != null) {
                    chapterDefinitions.put(chapterId, chapterJson.toString());
                } else {
                    Questlog.LOGGER.warn("Attempted to sync missing chapter definition: {}", chapterId);
                }
            }

            List<ResourceLocation> advancements = new ArrayList<>();
            for (AdvancementHolder holder : serverPlayer.getServer().getAdvancements().getAllAdvancements()) {
                advancements.add(holder.id());
            }

            Services.PLATFORM.sendPacketToClient(serverPlayer, new QuestSyncPacket(definitions, chapterDefinitions, data, advancements));
            Services.PLATFORM.sendPacketToClient(serverPlayer, new QuestEditModePacket(questManager.isEditMode()));
        }
    }

    private File getPlayerDataFile(Player player) {
        Path playerDataPath = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        return new File(playerDataPath.toFile(), player.getUUID() + ".questlog.dat");
    }

    private File getBackupPlayerDataFile(Player player) {
        Path playerDataPath = this.server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        return new File(playerDataPath.toFile(), player.getUUID() + ".questlog.dat_old");
    }
}
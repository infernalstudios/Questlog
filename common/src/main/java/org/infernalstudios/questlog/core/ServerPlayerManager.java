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
import org.infernalstudios.questlog.mixin.MinecraftServerAccessor;
import org.infernalstudios.questlog.mixin.PlayerDataStorageAccessor;
import org.infernalstudios.questlog.network.packet.QuestSyncPacket;
import org.infernalstudios.questlog.platform.Services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
        Questlog.LOGGER.debug("Saving player data for {}", questManager.player.getGameProfile().getName());
        CompoundTag data = new CompoundTag();
        for (Quest quest : questManager.getAllQuests()) {
            data.put(quest.getId().toString(), quest.serialize());
        }

        File playerDataFile = this.getPlayerDataFile(questManager.player);

        try {
            NbtIo.writeCompressed(data, playerDataFile.toPath());
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to save player data for {}", questManager.player.getGameProfile().getName());
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

        if (!playerDataFile.exists()) {
            try {
                NbtIo.writeCompressed(new CompoundTag(), playerDataFile.toPath());
            } catch (IOException e) {
                Questlog.LOGGER.error("Failed to create player data for {}", questManager.player.getGameProfile().getName());
                return;
            }
        }

        CompoundTag data;
        try {
            data = NbtIo.readCompressed(playerDataFile.toPath(), NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to load player data for {}", questManager.player.getGameProfile().getName());
            Questlog.LOGGER.error(e);
            return;
        }

        boolean shouldSave = false;
        questManager.reload();
        for (Quest quest : questManager.getAllQuests()) {
            if (data.contains(quest.getId().toString())) {
                CompoundTag questData = data.getCompound(quest.getId().toString());
                quest.deserialize(questData);
            } else {
                shouldSave = true;
            }
        }

        if (shouldSave) {
            this.save(questManager);
        }

        this.syncPlayer(questManager);
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
                chapterDefinitions.put(chapterId, DefinitionUtil.getCachedChapter(chapterId).toString());
            }

            Services.PLATFORM.sendPacketToClient(serverPlayer, new QuestSyncPacket(definitions, chapterDefinitions, data));
        }
    }

    private File getPlayerDataFile(Player player) {
        return new File(
                ((PlayerDataStorageAccessor) ((MinecraftServerAccessor) this.server).getPlayerDataStorage()).getPlayerDir(),
                player.getUUID() + ".questlog.dat"
        );
    }
}
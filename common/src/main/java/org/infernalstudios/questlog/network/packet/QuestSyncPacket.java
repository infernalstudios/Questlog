package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, String> chapterDefinitions,
                              Map<ResourceLocation, CompoundTag> data) {

    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;
    private static final Gson GSON = new GsonBuilder().create();
    private static QuestSyncPacket DEFERRED_PACKET = null;

    public static QuestSyncPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, String> defs = readMap(buf, FriendlyByteBuf::readUtf);
        Map<ResourceLocation, String> chapters = readMap(buf, FriendlyByteBuf::readUtf);
        Map<ResourceLocation, CompoundTag> data = readMap(buf, FriendlyByteBuf::readNbt);
        return new QuestSyncPacket(defs, chapters, data);
    }

    private static <V> Map<ResourceLocation, V> readMap(FriendlyByteBuf buf, Function<FriendlyByteBuf, V> valueReader) {
        Map<ResourceLocation, V> map = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            map.put(buf.readResourceLocation(), valueReader.apply(buf));
        }
        return map;
    }

    private static <V> void writeMap(FriendlyByteBuf buf, Map<ResourceLocation, V> map, BiConsumer<FriendlyByteBuf, V> valueWriter) {
        buf.writeVarInt(map.size());
        for (Map.Entry<ResourceLocation, V> entry : map.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            valueWriter.accept(buf, entry.getValue());
        }
    }

    public static void handle(QuestSyncPacket packet, IPacketContext ctx) {
        if (Minecraft.getInstance().player == null) {
            DEFERRED_PACKET = packet;
            return;
        }
        process(packet);
    }

    public static void handleDeferred() {
        if (DEFERRED_PACKET != null) {
            process(DEFERRED_PACKET);
            DEFERRED_PACKET = null;
        }
    }

    private static void process(QuestSyncPacket packet) {
        Questlog.LOGGER.info("Received quest & chapter sync from server.");

        DefinitionUtil.getCachedChapterKeys().clear();
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

    public void encode(FriendlyByteBuf buf) {
        writeMap(buf, this.definitions, FriendlyByteBuf::writeUtf);
        writeMap(buf, this.chapterDefinitions, FriendlyByteBuf::writeUtf);
        writeMap(buf, this.data, FriendlyByteBuf::writeNbt);
    }
}
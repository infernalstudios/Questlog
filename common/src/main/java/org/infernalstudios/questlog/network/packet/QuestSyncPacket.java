package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, String> chapterDefinitions,
                              Map<ResourceLocation, CompoundTag> data) implements CustomPacketPayload {
    public static final Type<QuestSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull QuestSyncPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            Map<ResourceLocation, String> defs = readMap(buf, ByteBufCodecs.STRING_UTF8);
            Map<ResourceLocation, String> chapters = readMap(buf, ByteBufCodecs.STRING_UTF8);
            Map<ResourceLocation, CompoundTag> data = readMap(buf, ByteBufCodecs.COMPOUND_TAG);
            return new QuestSyncPacket(defs, chapters, data);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, QuestSyncPacket packet) {
            writeMap(buf, packet.definitions(), ByteBufCodecs.STRING_UTF8);
            writeMap(buf, packet.chapterDefinitions(), ByteBufCodecs.STRING_UTF8);
            writeMap(buf, packet.data(), ByteBufCodecs.COMPOUND_TAG);
        }

        private <V> Map<ResourceLocation, V> readMap(RegistryFriendlyByteBuf buf, StreamCodec<? super RegistryFriendlyByteBuf, V> valueCodec) {
            Map<ResourceLocation, V> map = new HashMap<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                map.put(ResourceLocation.STREAM_CODEC.decode(buf), valueCodec.decode(buf));
            }
            return map;
        }

        private <V> void writeMap(RegistryFriendlyByteBuf buf, Map<ResourceLocation, V> map, StreamCodec<? super RegistryFriendlyByteBuf, V> valueCodec) {
            buf.writeVarInt(map.size());
            for (Map.Entry<ResourceLocation, V> entry : map.entrySet()) {
                ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
                valueCodec.encode(buf, entry.getValue());
            }
        }
    };

    private static final Gson GSON = new GsonBuilder().create();
    private static QuestSyncPacket DEFERRED_PACKET = null;

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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
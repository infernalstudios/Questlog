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
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, CompoundTag> data) implements CustomPacketPayload {
    public static final Type<QuestSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull QuestSyncPacket decode(RegistryFriendlyByteBuf buf) {
            Map<ResourceLocation, String> defs = new HashMap<>();
            int defSize = buf.readVarInt();
            for (int i = 0; i < defSize; i++) {
                defs.put(ResourceLocation.STREAM_CODEC.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf));
            }

            Map<ResourceLocation, CompoundTag> data = new HashMap<>();
            int dataSize = buf.readVarInt();
            for (int i = 0; i < dataSize; i++) {
                data.put(ResourceLocation.STREAM_CODEC.decode(buf), ByteBufCodecs.COMPOUND_TAG.decode(buf));
            }
            return new QuestSyncPacket(defs, data);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, QuestSyncPacket packet) {
            buf.writeVarInt(packet.definitions().size());
            for (Map.Entry<ResourceLocation, String> entry : packet.definitions().entrySet()) {
                ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
                ByteBufCodecs.STRING_UTF8.encode(buf, entry.getValue());
            }

            buf.writeVarInt(packet.data().size());
            for (Map.Entry<ResourceLocation, CompoundTag> entry : packet.data().entrySet()) {
                ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
                ByteBufCodecs.COMPOUND_TAG.encode(buf, entry.getValue());
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
        Questlog.LOGGER.info("Received full quest sync from server.");
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
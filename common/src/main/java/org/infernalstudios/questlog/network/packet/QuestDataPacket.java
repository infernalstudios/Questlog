package org.infernalstudios.questlog.network.packet;

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

public class QuestDataPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDataPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDataPacket::id,
            ByteBufCodecs.COMPOUND_TAG, QuestDataPacket::data,
            QuestDataPacket::new
    );

    private final ResourceLocation id;
    private final CompoundTag data;

    public QuestDataPacket(ResourceLocation id, CompoundTag data) {
        this.id = id;
        this.data = data;
    }

    public static void handle(QuestDataPacket packet, IPacketContext ctx) {
        try {
            QuestManager manager = QuestlogClient.getLocal();

            Quest quest = manager.getQuest(packet.id);
            if (quest == null) {
                throw new IllegalStateException("Quest is null, likely definition not loaded yet");
            }

            quest.deserialize(packet.data);
        } catch (Throwable e) {
            Questlog.LOGGER.error("Failed to handle QuestDataPacket", e);
        }
    }

    public ResourceLocation id() {
        return id;
    }

    public CompoundTag data() {
        return data;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
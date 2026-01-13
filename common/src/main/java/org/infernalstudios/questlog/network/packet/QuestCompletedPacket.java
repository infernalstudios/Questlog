package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogEvents;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

public class QuestCompletedPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestCompletedPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "completed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestCompletedPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestCompletedPacket::id,
            QuestCompletedPacket::new
    );

    private final ResourceLocation id;

    public QuestCompletedPacket(ResourceLocation id) {
        this.id = id;
    }

    public static void handle(QuestCompletedPacket packet, IPacketContext ctx) {
        QuestManager manager = QuestlogClient.getLocal();
        QuestlogEvents.onQuestCompleted(new QuestEvent.Completed(manager.player, manager.getQuest(packet.id), false));
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
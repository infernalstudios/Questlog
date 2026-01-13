package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

public class QuestRemovePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestRemovePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "remove"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestRemovePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestRemovePacket::id,
            QuestRemovePacket::new
    );

    private final ResourceLocation id;

    public QuestRemovePacket(ResourceLocation id) {
        this.id = id;
    }

    public static void handle(QuestRemovePacket packet, IPacketContext ctx) {
        Questlog.LOGGER.trace("Received remove packet for quest {}", packet.id.toString());
        QuestManager manager = QuestlogClient.getLocal();
        manager.removeQuest(packet.id);
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
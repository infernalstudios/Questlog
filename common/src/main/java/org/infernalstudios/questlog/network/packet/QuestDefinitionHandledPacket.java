package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

public class QuestDefinitionHandledPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestDefinitionHandledPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "definition_handled"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDefinitionHandledPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDefinitionHandledPacket::id,
            QuestDefinitionHandledPacket::new
    );

    private final ResourceLocation id;

    public QuestDefinitionHandledPacket(ResourceLocation id) {
        this.id = id;
    }

    public static void handle(QuestDefinitionHandledPacket packet, IPacketContext ctx) {
        Questlog.LOGGER.trace("Client handled definition for quest {}", packet.id.toString());

        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(ctx.getSender());
        manager.sync(packet.id);
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;

public record QuestTriggeredPacket(ResourceLocation id) implements CustomPacketPayload {
    public static final Type<QuestTriggeredPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "triggered"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestTriggeredPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestTriggeredPacket::id,
            QuestTriggeredPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
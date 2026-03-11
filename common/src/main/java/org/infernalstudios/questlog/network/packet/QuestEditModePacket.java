package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;

public record QuestEditModePacket(boolean enabled) implements CustomPacketPayload {
    public static final Type<QuestEditModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "edit_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestEditModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, QuestEditModePacket::enabled,
            QuestEditModePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
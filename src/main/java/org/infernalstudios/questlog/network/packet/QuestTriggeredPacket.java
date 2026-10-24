package org.infernalstudios.questlog.network.packet;

import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;
*///?}

//? if >=1.21 {
public record QuestTriggeredPacket(ResourceLocation id) implements CustomPacketPayload {
//?} else {
//public record QuestTriggeredPacket(ResourceLocation id) {
//?}
    //? if >=1.21 {
    public static final Type<QuestTriggeredPacket> TYPE = new Type<>(Questlog.id("triggered"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestTriggeredPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestTriggeredPacket::id,
            QuestTriggeredPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestTriggeredPacket decode(FriendlyByteBuf buf) {
        return new QuestTriggeredPacket(buf.readResourceLocation());
    }

    public static void handle(QuestTriggeredPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
    *///?}

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

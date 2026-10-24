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
public record QuestRemovePacket(ResourceLocation id) implements CustomPacketPayload {
//?} else {
//public record QuestRemovePacket(ResourceLocation id) {
//?}
    //? if >=1.21 {
    public static final Type<QuestRemovePacket> TYPE = new Type<>(Questlog.id("remove"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestRemovePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestRemovePacket::id,
            QuestRemovePacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestRemovePacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        return new QuestRemovePacket(id);
    }

    public static void handle(QuestRemovePacket packet, IPacketContext ctx) {
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

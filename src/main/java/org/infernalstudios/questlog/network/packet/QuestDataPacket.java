package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
public record QuestDataPacket(ResourceLocation id, CompoundTag data) implements CustomPacketPayload {
//?} else {
//public record QuestDataPacket(ResourceLocation id, CompoundTag data) {
//?}
    //? if >=1.21 {
    public static final Type<QuestDataPacket> TYPE = new Type<>(Questlog.id("data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDataPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDataPacket::id,
            ByteBufCodecs.COMPOUND_TAG, QuestDataPacket::data,
            QuestDataPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestDataPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        CompoundTag data = buf.readNbt();

        return new QuestDataPacket(id, data);
    }

    public static void handle(QuestDataPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeNbt(this.data);
    }
    *///?}

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

package org.infernalstudios.questlog.network.packet;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.IPacketContext;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
//?} else {
//import net.minecraft.network.FriendlyByteBuf;
//?}

import java.util.Objects;

//? if >=1.21 {
public record QuestReadPacket(ResourceLocation id) implements CustomPacketPayload {
//?} else {
//public record QuestReadPacket(ResourceLocation id) {
//?}
    //? if >=1.21 {
    public static final Type<QuestReadPacket> TYPE = new Type<>(Questlog.id("read"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestReadPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestReadPacket::id,
            QuestReadPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestReadPacket decode(FriendlyByteBuf buf) {
        return new QuestReadPacket(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
    *///?}

    public static void handle(QuestReadPacket packet, IPacketContext ctx) {
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(Objects.requireNonNull(ctx.getSender()));
        Questlog.EVENTS.post(new QuestEvent.Read(manager.player, manager.getQuest(packet.id), true));
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

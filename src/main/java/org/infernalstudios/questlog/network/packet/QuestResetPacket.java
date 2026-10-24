package org.infernalstudios.questlog.network.packet;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
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
public record QuestResetPacket(ResourceLocation id) implements CustomPacketPayload {
//?} else {
//public record QuestResetPacket(ResourceLocation id) {
//?}
    //? if >=1.21 {
    public static final Type<QuestResetPacket> TYPE = new Type<>(Questlog.id("reset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestResetPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestResetPacket::id,
            QuestResetPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestResetPacket decode(FriendlyByteBuf buf) {
        return new QuestResetPacket(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
    *///?}

    public static void handle(QuestResetPacket packet, IPacketContext ctx) {
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(Objects.requireNonNull(ctx.getSender()));
        Quest quest = manager.getQuest(packet.id);
        if (quest != null && quest.isRepeatable()) {
            if (quest.isGlobal()) {
                ServerPlayerManager.INSTANCE.resetGlobalQuest(quest.getId());
            } else {
                quest.resetProgress();
                ServerPlayerManager.INSTANCE.save(manager);
            }
        }
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

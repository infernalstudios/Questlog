package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

public record QuestOpenPacket(String target) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestOpenPacket decode(FriendlyByteBuf buf) {
        return new QuestOpenPacket(buf.readUtf());
    }

    public static void handle(QuestOpenPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.target);
    }
}
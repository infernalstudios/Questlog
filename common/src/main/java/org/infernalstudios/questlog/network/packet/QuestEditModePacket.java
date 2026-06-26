package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

public record QuestEditModePacket(boolean enabled) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestEditModePacket decode(FriendlyByteBuf buf) {
        return new QuestEditModePacket(buf.readBoolean());
    }

    public static void handle(QuestEditModePacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.enabled);
    }
}
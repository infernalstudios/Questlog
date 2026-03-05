package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.IPacketContext;

public record QuestEditModePacket(boolean enabled) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestEditModePacket decode(FriendlyByteBuf buf) {
        return new QuestEditModePacket(buf.readBoolean());
    }

    public static void handle(QuestEditModePacket packet, IPacketContext ctx) {
        Questlog.LOGGER.info("Questlog Edit Mode has been set to: {}", packet.enabled());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.enabled);
    }
}
package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

public class QuestTriggeredPacket {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    private final ResourceLocation id;

    public QuestTriggeredPacket(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() { return this.id; }

    public static QuestTriggeredPacket decode(FriendlyByteBuf buf) {
        return new QuestTriggeredPacket(buf.readResourceLocation());
    }

    public static void handle(QuestTriggeredPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
}

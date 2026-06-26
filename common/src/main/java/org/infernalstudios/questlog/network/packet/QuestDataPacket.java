package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

public record QuestDataPacket(ResourceLocation id, CompoundTag data) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

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
}

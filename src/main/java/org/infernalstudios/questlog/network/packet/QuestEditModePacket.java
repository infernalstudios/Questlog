package org.infernalstudios.questlog.network.packet;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;
*///?}

//? if >=1.21 {
public record QuestEditModePacket(boolean enabled) implements CustomPacketPayload {
//?} else {
//public record QuestEditModePacket(boolean enabled) {
//?}
    //? if >=1.21 {
    public static final Type<QuestEditModePacket> TYPE = new Type<>(Questlog.id("edit_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestEditModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, QuestEditModePacket::enabled,
            QuestEditModePacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestEditModePacket decode(FriendlyByteBuf buf) {
        return new QuestEditModePacket(buf.readBoolean());
    }

    public static void handle(QuestEditModePacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.enabled);
    }
    *///?}

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

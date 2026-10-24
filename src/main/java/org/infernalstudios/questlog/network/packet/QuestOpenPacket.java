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
public record QuestOpenPacket(String target) implements CustomPacketPayload {
//?} else {
//public record QuestOpenPacket(String target) {
//?}
    //? if >=1.21 {
    public static final Type<QuestOpenPacket> TYPE = new Type<>(Questlog.id("open_ui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestOpenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestOpenPacket::target,
            QuestOpenPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestOpenPacket decode(FriendlyByteBuf buf) {
        return new QuestOpenPacket(buf.readUtf());
    }

    public static void handle(QuestOpenPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.target);
    }
    *///?}

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
public class QuestDefinitionPacket implements CustomPacketPayload {
//?} else {
//public class QuestDefinitionPacket {
//?}
    private static final Gson GSON = new GsonBuilder().create();

    //? if >=1.21 {
    public static final CustomPacketPayload.Type<QuestDefinitionPacket> TYPE = new CustomPacketPayload.Type<>(Questlog.id("definition"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDefinitionPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDefinitionPacket::id,
            ByteBufCodecs.STRING_UTF8, QuestDefinitionPacket::getJsonString,
            QuestDefinitionPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestDefinitionPacket decode(FriendlyByteBuf buf) {
        return new QuestDefinitionPacket(buf.readResourceLocation(), buf.readUtf());
    }

    public static void handle(QuestDefinitionPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeUtf(this.getJsonString());
    }
    *///?}

    private final ResourceLocation id;
    private final JsonObject definition;

    public QuestDefinitionPacket(ResourceLocation id, String json) {
        this(id, GSON.fromJson(json, JsonObject.class));
    }

    public QuestDefinitionPacket(ResourceLocation id, JsonObject definition) {
        this.id = id;
        this.definition = definition;
    }

    public ResourceLocation id() {
        return id;
    }

    public String getJsonString() {
        return GSON.toJson(this.definition);
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

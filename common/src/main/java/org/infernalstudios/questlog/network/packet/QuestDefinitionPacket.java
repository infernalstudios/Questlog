package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

public class QuestDefinitionPacket {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;
    private static final Gson GSON = new GsonBuilder().create();

    private final ResourceLocation id;
    private final JsonObject definition;

    public QuestDefinitionPacket(ResourceLocation id, String json) {
        this(id, GSON.fromJson(json, JsonObject.class));
    }

    public QuestDefinitionPacket(ResourceLocation id, JsonObject definition) {
        this.id = id;
        this.definition = definition;
    }

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

    public ResourceLocation id() {
        return id;
    }

    public String getJsonString() {
        return GSON.toJson(this.definition);
    }
}
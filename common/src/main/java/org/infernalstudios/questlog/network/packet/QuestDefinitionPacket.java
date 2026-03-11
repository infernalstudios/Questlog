package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;

public class QuestDefinitionPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestDefinitionPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "definition"));
    private static final Gson GSON = new GsonBuilder().create();

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDefinitionPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDefinitionPacket::id,
            ByteBufCodecs.STRING_UTF8, QuestDefinitionPacket::getJsonString,
            QuestDefinitionPacket::new
    );
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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
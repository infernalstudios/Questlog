package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.platform.Services;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestDefinitionPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestDefinitionPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "definition"));
    private static final Gson GSON = new GsonBuilder().create();

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestDefinitionPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestDefinitionPacket::id,
            ByteBufCodecs.STRING_UTF8, QuestDefinitionPacket::getJsonString,
            QuestDefinitionPacket::new
    );
    private static final List<QuestDefinitionPacket> DEFERRED = new CopyOnWriteArrayList<>();
    private final ResourceLocation id;
    private final JsonObject definition;

    public QuestDefinitionPacket(ResourceLocation id, String json) {
        this(id, GSON.fromJson(json, JsonObject.class));
    }

    public QuestDefinitionPacket(ResourceLocation id, JsonObject definition) {
        this.id = id;
        this.definition = definition;
    }

    public static void handle(QuestDefinitionPacket packet, IPacketContext ctx) {
        if (Minecraft.getInstance().player == null) {
            defer(packet);
            return;
        }
        try {
            QuestManager manager = QuestlogClient.getLocal();

            Quest quest = Quest.create(Objects.requireNonNull(packet.definition), packet.id, manager);
            manager.addQuest(quest);

            Services.PLATFORM.sendPacketToServer(new QuestDefinitionHandledPacket(packet.id));
        } catch (Throwable e) {
            Questlog.LOGGER.error("Failed to handle QuestDefinitionPacket", e);
        }
    }

    private static void defer(QuestDefinitionPacket packet) {
        DEFERRED.add(packet);
    }

    public static void handleDeferred() {
        Questlog.LOGGER.debug("Handling deferred QuestDefinitionPackets");
        for (QuestDefinitionPacket packet : DEFERRED) {
            handle(packet, null);
        }
        DEFERRED.clear();
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
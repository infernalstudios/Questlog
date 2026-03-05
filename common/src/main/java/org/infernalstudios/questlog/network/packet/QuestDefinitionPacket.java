package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class QuestDefinitionPacket {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;
    private static final Gson GSON = new GsonBuilder().create();

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

    public static QuestDefinitionPacket decode(FriendlyByteBuf buf) {
        return new QuestDefinitionPacket(buf.readResourceLocation(), buf.readUtf());
    }

    public static void handle(QuestDefinitionPacket packet, IPacketContext ctx) {
        if (Minecraft.getInstance().player == null) {
            defer(packet);
            return;
        }
        try {
            QuestManager manager = QuestlogClient.getLocal();

            Quest existing = manager.getQuest(packet.id());
            CompoundTag savedData = existing != null ? existing.serialize() : null;

            Quest quest = Quest.create(Objects.requireNonNull(packet.definition), packet.id(), manager);
            if (savedData != null) {
                quest.deserialize(savedData);
            }

            manager.addQuest(quest);
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
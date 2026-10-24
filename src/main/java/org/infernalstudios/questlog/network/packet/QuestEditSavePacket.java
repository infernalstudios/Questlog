package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.platform.Services;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
//?} else {
//import net.minecraft.network.FriendlyByteBuf;
//?}

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

//? if >=1.21 {
public record QuestEditSavePacket(ResourceLocation id, String json) implements CustomPacketPayload {
//?} else {
//public record QuestEditSavePacket(ResourceLocation id, String json) {
//?}
    //? if >=1.21 {
    public static final Type<QuestEditSavePacket> TYPE = new Type<>(Questlog.id("edit_save"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestEditSavePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestEditSavePacket::id,
            ByteBufCodecs.STRING_UTF8, QuestEditSavePacket::json,
            QuestEditSavePacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestEditSavePacket decode(FriendlyByteBuf buf) {
        return new QuestEditSavePacket(buf.readResourceLocation(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeUtf(this.json);
    }
    *///?}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void handle(QuestEditSavePacket packet, IPacketContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.getSender();
        if (player == null || !player.hasPermissions(2)) {
            Questlog.LOGGER.warn("Player {} tried to edit quest without permissions", player != null ? player.getGameProfile().getName() : "null");
            return;
        }

        try {
            JsonObject definition = GSON.fromJson(packet.json, JsonObject.class);
            Path configDir = Services.PLATFORM.getConfigDirectory().resolve("questlog");
            Path questDir = configDir.resolve("quests");
            Path filePath = questDir.resolve(packet.id.getPath() + ".json");
            Files.createDirectories(filePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                GSON.toJson(definition, writer);
            }
            Questlog.LOGGER.info("Saved quest definition for {} to {}", packet.id, filePath);

            DefinitionUtil.loadFromConfig();

            if (ServerPlayerManager.INSTANCE != null) {
                for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(onlinePlayer);
                    manager.reload();
                    ServerPlayerManager.INSTANCE.syncPlayer(manager);
                }
            }
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to save quest definition", e);
        }
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

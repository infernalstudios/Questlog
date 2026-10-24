package org.infernalstudios.questlog.network.packet;

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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
//?} else {
//import net.minecraft.network.FriendlyByteBuf;
//?}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//? if >=1.21 {
public record QuestEditRemovePacket(ResourceLocation id) implements CustomPacketPayload {
//?} else {
//public record QuestEditRemovePacket(ResourceLocation id) {
//?}
    //? if >=1.21 {
    public static final Type<QuestEditRemovePacket> TYPE = new Type<>(Questlog.id("edit_remove"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestEditRemovePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestEditRemovePacket::id,
            QuestEditRemovePacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestEditRemovePacket decode(FriendlyByteBuf buf) {
        return new QuestEditRemovePacket(buf.readResourceLocation());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
    *///?}

    public static void handle(QuestEditRemovePacket packet, IPacketContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.getSender();
        if (player == null || !player.hasPermissions(2)) {
            Questlog.LOGGER.warn("Player {} tried to remove quest without permissions", player != null ? player.getGameProfile().getName() : "null");
            return;
        }

        try {
            Path configDir = Services.PLATFORM.getConfigDirectory().resolve("questlog");
            Path questDir = configDir.resolve("quests");
            Path filePath = questDir.resolve(packet.id.getPath() + ".json");
            if (Files.deleteIfExists(filePath)) {
                Questlog.LOGGER.info("Deleted quest definition file for {}", packet.id);
            } else {
                Questlog.LOGGER.warn("Tried to delete quest definition file {} but it did not exist", filePath);
            }

            DefinitionUtil.loadFromConfig();

            if (ServerPlayerManager.INSTANCE != null) {
                for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(onlinePlayer);
                    manager.reload();
                    ServerPlayerManager.INSTANCE.syncPlayer(manager);
                }
            }
        } catch (IOException e) {
            Questlog.LOGGER.error("Failed to delete quest definition", e);
        }
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

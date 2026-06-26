package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record ChapterEditRemovePacket(ResourceLocation id) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static ChapterEditRemovePacket decode(FriendlyByteBuf buf) {
        return new ChapterEditRemovePacket(buf.readResourceLocation());
    }

    public static void handle(ChapterEditRemovePacket packet, IPacketContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.getSender();
        if (player == null || !player.hasPermissions(2)) {
            Questlog.LOGGER.warn("Player {} tried to remove chapter without permissions", player != null ? player.getGameProfile().getName() : "null");
            return;
        }

        try {
            Path configDir = Services.PLATFORM.getConfigDirectory().resolve("questlog");
            Path chapterDir = configDir.resolve("chapters");
            Path filePath = chapterDir.resolve(packet.id.getPath() + ".json");
            if (Files.deleteIfExists(filePath)) {
                Questlog.LOGGER.info("Deleted chapter definition file for {}", packet.id);
            } else {
                Questlog.LOGGER.warn("Tried to delete chapter definition file {} but it did not exist", filePath);
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
            Questlog.LOGGER.error("Failed to delete chapter definition", e);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
}

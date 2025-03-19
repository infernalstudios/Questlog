package org.infernalstudios.questlog.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.infernalstudios.questlog.networking.QuestlogPacketsForge;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public <T> void sendPacketToClient(ServerPlayer player, T packet) {
        QuestlogPacketsForge.sendToPlayer(packet, player);
    }

    @Override
    public <T> void sendPacketToServer(T packet) {
        QuestlogPacketsForge.sendToServer(packet);
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
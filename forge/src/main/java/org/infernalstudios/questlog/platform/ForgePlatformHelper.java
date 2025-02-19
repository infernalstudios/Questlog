package org.infernalstudios.questlog.platform;

import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.networking.QuestlogPacketsForge;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public <T> void sendPacketToClient(ServerPlayer player, T packet) {
        QuestlogPacketsForge.sendToPlayer(packet, player);
    }

    @Override
    public <T> void sendPacketToServer(T packet) {
        QuestlogPacketsForge.sendToServer(packet);
    }
}
package org.infernalstudios.questlog.platform.services;

import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {
    <T> void sendPacketToClient(ServerPlayer player, T packet);

    <T> void sendPacketToServer(T packet);

    Path getConfigDirectory();
}
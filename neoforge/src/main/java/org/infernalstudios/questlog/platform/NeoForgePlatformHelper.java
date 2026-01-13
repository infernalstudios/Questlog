package org.infernalstudios.questlog.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public <T> void sendPacketToClient(ServerPlayer player, T packet) {
        if (packet instanceof CustomPacketPayload payload) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @Override
    public <T> void sendPacketToServer(T packet) {
        if (packet instanceof CustomPacketPayload payload) {
            PacketDistributor.sendToServer(payload);
        }
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
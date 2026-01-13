package org.infernalstudios.questlog.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public <T> void sendPacketToClient(ServerPlayer player, T packet) {
        if (packet instanceof CustomPacketPayload payload) {
            ServerPlayNetworking.send(player, payload);
        } else {
            throw new IllegalArgumentException("Packet must implement CustomPacketPayload");
        }
    }

    @Override
    public <T> void sendPacketToServer(T packet) {
        if (packet instanceof CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
        } else {
            throw new IllegalArgumentException("Packet must implement CustomPacketPayload");
        }
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
package org.infernalstudios.questlog.platform;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    @Override
    public boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        if (!FabricLoader.getInstance().isModLoaded("origins")) return false;
        try {
            var component = ModComponents.ORIGIN.get(player);
            for (Origin origin : component.getOrigins().values()) {
                if (ResourceLocation.parse(origin.getId().toString()).equals(originId)) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public Collection<ResourceLocation> getOriginIds() {
        if (!FabricLoader.getInstance().isModLoaded("origins")) return Collections.emptyList();
        try {
            List<ResourceLocation> result = new ArrayList<>();
            for (var id : OriginManager.keySet()) {
                result.add(ResourceLocation.parse(id.toString()));
            }
            return result;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
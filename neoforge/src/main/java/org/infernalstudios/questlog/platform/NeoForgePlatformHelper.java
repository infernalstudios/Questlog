package org.infernalstudios.questlog.platform;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.origin.OriginRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    @Override
    public boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        if (!ModList.get().isLoaded("origins")) return false;
        try {
            OriginDataHolder holder = OriginDataHolder.get(player);
            for (Holder<?> origin : holder.getOrigins().values()) {
                ResourceKey<?> key = origin.unwrapKey().orElse(null);
                if (key != null && key.location().equals(originId)) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public Collection<ResourceLocation> getOriginIds() {
        if (!ModList.get().isLoaded("origins")) return Collections.emptyList();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return Collections.emptyList();
        try {
            List<ResourceLocation> result = new ArrayList<>();
            OriginRegistries.streamAvailableOrigins(mc.level.registryAccess())
                    .forEach(holder -> holder.unwrapKey()
                            .ifPresent(key -> result.add(key.location())));
            return result;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
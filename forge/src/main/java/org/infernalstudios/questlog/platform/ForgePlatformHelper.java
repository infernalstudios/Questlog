package org.infernalstudios.questlog.platform;

import io.github.edwinmindcraft.origins.api.OriginsAPI;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.infernalstudios.questlog.networking.QuestlogPacketsForge;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

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

    @Override
    public boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        if (!ModList.get().isLoaded("origins")) return false;

        return IOriginContainer.get(player).map(container -> {
            return container.getOrigins().values().stream()
                    .anyMatch(originKey -> originKey.location().equals(originId));
        }).orElse(false);
    }

    @Override
    public Collection<ResourceLocation> getOriginIds() {
        if (!ModList.get().isLoaded("origins")) return Collections.emptyList();

        try {
            return OriginsAPI.getOriginsRegistry().keySet();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
package org.infernalstudios.questlog.platform;

//? if fabric {
/*import io.github.apace100.origins.origin.Origin;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;
//? if >=1.21 {
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.infernalstudios.questlog.Questlog;
//?} else {
/^import io.github.apace100.origins.origin.OriginRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.QuestlogPackets;
^///?}

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
//? if >=1.21 {
import java.util.ArrayList;
import java.util.List;
//?} else {
/^import java.util.HashMap;
import java.util.Map;
^///?}

public class FabricPlatformHelper implements IPlatformHelper {
    //? if >=1.21 {
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
    //?} else {
    /^private static final Map<Class<?>, QuestlogPackets.RegisteredPacket<?>> classToIdCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> QuestlogPackets.RegisteredPacket<T> getRegisteredPacketByClass(Class<T> packetClass) {
        return (QuestlogPackets.RegisteredPacket<T>) classToIdCache.computeIfAbsent(packetClass, clazz ->
                QuestlogPackets.PACKETS.stream()
                        .filter(registeredPacket -> registeredPacket.clazz().equals(clazz))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No packet registered for class " + clazz))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void sendPacketToClient(ServerPlayer player, T packet) {
        QuestlogPackets.RegisteredPacket<T> registeredPacket = getRegisteredPacketByClass((Class<T>) packet.getClass());
        if (registeredPacket.direction() != IPacketContext.Direction.SERVER_TO_CLIENT) {
            throw new IllegalArgumentException("Packet " + registeredPacket.id() + " is not a server-to-client packet");
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        registeredPacket.encoder().accept(packet, buf);

        ServerPlayNetworking.send(player, registeredPacket.id(), buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void sendPacketToServer(T packet) {
        QuestlogPackets.RegisteredPacket<T> registeredPacket = getRegisteredPacketByClass((Class<T>) packet.getClass());
        if (registeredPacket.direction() != IPacketContext.Direction.CLIENT_TO_SERVER) {
            throw new IllegalArgumentException("Packet " + registeredPacket.id() + " is not a client-to-server packet");
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        registeredPacket.encoder().accept(packet, buf);

        ClientPlayNetworking.send(registeredPacket.id(), buf);
    }
    ^///?}

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        if (!FabricLoader.getInstance().isModLoaded("origins")) return false;
        try {
            //? if >=1.21 {
            var component = ModComponents.ORIGIN.get(player);
            for (Origin origin : component.getOrigins().values()) {
                if (Questlog.parseId(origin.getId().toString()).equals(originId)) return true;
            }
            //?} else {
            /^for (Origin origin : Origin.get(player).values()) {
                if (origin.getIdentifier().equals(originId)) return true;
            }
            ^///?}
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public Collection<ResourceLocation> getOriginIds() {
        if (!FabricLoader.getInstance().isModLoaded("origins")) return Collections.emptyList();
        try {
            //? if >=1.21 {
            List<ResourceLocation> result = new ArrayList<>();
            for (var id : OriginManager.keySet()) {
                result.add(Questlog.parseId(id.toString()));
            }
            return result;
            //?} else
            //return OriginRegistry.identifiers().toList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
*///?}

package org.infernalstudios.questlog.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.QuestlogPackets;
import org.infernalstudios.questlog.platform.services.IPlatformHelper;

import java.util.HashMap;
import java.util.Map;

public class FabricPlatformHelper implements IPlatformHelper {
    private static final Map<Class<?>, QuestlogPackets.RegisteredPacket<?>> classToIdCache = new HashMap<>();

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

    @SuppressWarnings("unchecked")
    private static <T> QuestlogPackets.RegisteredPacket<T> getRegisteredPacketByClass(Class<T> packetClass) {
        return (QuestlogPackets.RegisteredPacket<T>) classToIdCache.computeIfAbsent(packetClass, clazz ->
            QuestlogPackets.PACKETS.stream()
                .filter(registeredPacket -> registeredPacket.clazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No packet registered for class " + clazz))
        );
    }
}

package org.infernalstudios.questlog.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.QuestlogPackets;

public class QuestlogPacketsFabric {
    public static void registerCommon() {
        for (QuestlogPackets.RegisteredPacket<?> packet : QuestlogPackets.PACKETS) {
            if (packet.direction() == IPacketContext.Direction.CLIENT_TO_SERVER) {
                registerC2S(packet);
            }
        }
    }

    public static void registerClient() {
        for (QuestlogPackets.RegisteredPacket<?> packet : QuestlogPackets.PACKETS) {
            if (packet.direction() == IPacketContext.Direction.SERVER_TO_CLIENT) {
                registerS2C(packet);
            }
        }
    }

    private static <T> void registerC2S(QuestlogPackets.RegisteredPacket<T> packet) {
        ServerPlayNetworking.registerGlobalReceiver(packet.id(), (server, player, handler, buf, responseSender) -> {
            T decoded = packet.decoder().apply(buf);
            server.execute(() -> packet.handler().accept(decoded, createServerContext(player)));
        });
    }

    private static <T> void registerS2C(QuestlogPackets.RegisteredPacket<T> packet) {
        ClientPlayNetworking.registerGlobalReceiver(packet.id(), (client, handler, buf, responseSender) -> {
            T decoded = packet.decoder().apply(buf);
            client.execute(() -> packet.handler().accept(decoded, createClientContext()));
        });
    }

    private static IPacketContext createClientContext() {
        return new IPacketContext() {
            @Override
            public Player getSender() {
                return null;
            }

            @Override
            public Direction getDirection() {
                return Direction.SERVER_TO_CLIENT;
            }
        };
    }

    private static IPacketContext createServerContext(Player player) {
        return new IPacketContext() {
            @Override
            public Player getSender() {
                return player;
            }

            @Override
            public Direction getDirection() {
                return Direction.CLIENT_TO_SERVER;
            }
        };
    }
}
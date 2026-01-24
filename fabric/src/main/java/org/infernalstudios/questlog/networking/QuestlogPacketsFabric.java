package org.infernalstudios.questlog.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.mixin.fabric.ServerGamePacketListenerImplAccessor;
import org.infernalstudios.questlog.mixin.fabric.client.ClientPacketListenerAccessor;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.QuestlogPackets;

public class QuestlogPacketsFabric {

    public static void registerCommon() {
        for (QuestlogPackets.RegisteredPacket<?> packet : QuestlogPackets.PACKETS) {
            if (packet.direction() == IPacketContext.Direction.CLIENT_TO_SERVER) {
                registerC2SPacket(packet);
            }
        }
    }

    public static void registerClient() {
        for (QuestlogPackets.RegisteredPacket<?> packet : QuestlogPackets.PACKETS) {
            if (packet.direction() == IPacketContext.Direction.SERVER_TO_CLIENT) {
                registerS2CPacket(packet);
            }
        }
    }

    private static <T> void registerS2CPacket(QuestlogPackets.RegisteredPacket<T> registered) {
        ClientPlayNetworking.registerGlobalReceiver(registered.id(), (client, handler, buf, responseSender) -> {
            T packet = registered.decoder().apply(buf);
            ((ClientPacketListenerAccessor) handler).getMinecraft().execute(() ->
                    registered.handler().accept(packet, new IPacketContext() {
                        @Override
                        public Player getSender() {
                            return null;
                        }

                        @Override
                        public Direction getDirection() {
                            return Direction.SERVER_TO_CLIENT;
                        }
                    })
            );
        });
    }

    private static <T> void registerC2SPacket(QuestlogPackets.RegisteredPacket<T> registered) {
        ServerPlayNetworking.registerGlobalReceiver(registered.id(), (server, player, handler, buf, responseSender) -> {
            T packet = registered.decoder().apply(buf);
            ((ServerGamePacketListenerImplAccessor) handler).getServer().execute(() ->
                    registered.handler().accept(packet, new IPacketContext() {
                        @Override
                        public Player getSender() {
                            return player;
                        }

                        @Override
                        public Direction getDirection() {
                            return Direction.CLIENT_TO_SERVER;
                        }
                    })
            );
        });
    }
}
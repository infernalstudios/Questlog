package org.infernalstudios.questlog.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.QuestlogPackets;

import java.util.Optional;

public class QuestlogPacketsForge {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Questlog.MODID, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    public static synchronized void register() {
        for (int i = 0; i < QuestlogPackets.PACKETS.size(); i++) {
            QuestlogPackets.RegisteredPacket<?> packet = QuestlogPackets.PACKETS.get(i);
            registerPacket(i, packet);
        }
    }

    private static synchronized <T> void registerPacket(int index, QuestlogPackets.RegisteredPacket<T> registered) {
        CHANNEL.messageBuilder(registered.clazz(), index, registered.direction() == IPacketContext.Direction.CLIENT_TO_SERVER ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT)
            .encoder(registered.encoder())
            .decoder(registered.decoder())
            .consumerMainThread((packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (registered.direction() == IPacketContext.Direction.CLIENT_TO_SERVER) {
                    if (!context.getDirection().getReceptionSide().isServer()) {
                        throw new IllegalStateException("Received a client to server packet on the wrong side " + context.getDirection() + ": " + registered.id());
                    }

                    if (contextSupplier.get().getSender() == null) {
                        throw new IllegalStateException("Received a client to server packet with a null sender: " + registered.id());
                    }
                }

                if (registered.direction() == IPacketContext.Direction.SERVER_TO_CLIENT) {
                    if (!context.getDirection().getReceptionSide().isClient()) {
                        throw new IllegalStateException("Received a server to client packet on the wrong side " + context.getDirection() + ": " + registered.id());
                    }
                }

                registered.handler().accept(packet, new IPacketContext() {
                    @Override
                    public ServerPlayer getSender() {
                        return contextSupplier.get().getSender();
                    }

                    @Override
                    public IPacketContext.Direction getDirection() {
                        return registered.direction();
                    }
                });
            })
            .add();
    }

    public static <M> void sendToPlayer(M message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <M> void sendToServer(M message) {
        CHANNEL.sendToServer(message);
    }
}

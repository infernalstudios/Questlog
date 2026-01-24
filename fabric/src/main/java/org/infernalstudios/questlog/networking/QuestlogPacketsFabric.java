package org.infernalstudios.questlog.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.packet.*;

public class QuestlogPacketsFabric {
    public static void registerCommon() {
        // Register S2C Payloads
        PayloadTypeRegistry.playS2C().register(QuestDataPacket.TYPE, QuestDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestDefinitionPacket.TYPE, QuestDefinitionPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestRemovePacket.TYPE, QuestRemovePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestTriggeredPacket.TYPE, QuestTriggeredPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestCompletedPacket.TYPE, QuestCompletedPacket.STREAM_CODEC);

        // Register C2S Payloads
        PayloadTypeRegistry.playC2S().register(QuestDefinitionHandledPacket.TYPE, QuestDefinitionHandledPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuestRewardCollectPacket.TYPE, QuestRewardCollectPacket.STREAM_CODEC);

        // Register Server Receivers
        registerServerReceivers();
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(QuestDataPacket.TYPE, (payload, context) -> context.client().execute(() -> QuestDataPacket.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestDefinitionPacket.TYPE, (payload, context) -> context.client().execute(() -> QuestDefinitionPacket.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestRemovePacket.TYPE, (payload, context) -> context.client().execute(() -> QuestRemovePacket.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestTriggeredPacket.TYPE, (payload, context) -> context.client().execute(() -> QuestTriggeredPacket.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestCompletedPacket.TYPE, (payload, context) -> context.client().execute(() -> QuestCompletedPacket.handle(payload, createClientContext())));
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(QuestDefinitionHandledPacket.TYPE, (payload, context) -> context.server().execute(() -> QuestDefinitionHandledPacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuestRewardCollectPacket.TYPE, (payload, context) -> context.server().execute(() -> QuestRewardCollectPacket.handle(payload, createServerContext(context.player()))));
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
package org.infernalstudios.questlog.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.packet.*;

public class QuestlogPacketsFabric {
    public static void registerCommon() {
        // Register S2C Payloads
        PayloadTypeRegistry.playS2C().register(QuestSyncPacket.TYPE, QuestSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestDataPacket.TYPE, QuestDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestDefinitionPacket.TYPE, QuestDefinitionPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestRemovePacket.TYPE, QuestRemovePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestTriggeredPacket.TYPE, QuestTriggeredPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestCompletedPacket.TYPE, QuestCompletedPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestOpenPacket.TYPE, QuestOpenPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(QuestEditModePacket.TYPE, QuestEditModePacket.STREAM_CODEC);

        // Register C2S Payloads
        PayloadTypeRegistry.playC2S().register(QuestRewardCollectPacket.TYPE, QuestRewardCollectPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuestReadPacket.TYPE, QuestReadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuestEditSavePacket.TYPE, QuestEditSavePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuestEditRemovePacket.TYPE, QuestEditRemovePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ChapterEditSavePacket.TYPE, ChapterEditSavePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ChapterEditRemovePacket.TYPE, ChapterEditRemovePacket.STREAM_CODEC);

        // Register Server Receivers
        registerServerReceivers();
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(QuestSyncPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestDataPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestDefinitionPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestRemovePacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestTriggeredPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestCompletedPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestOpenPacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
        ClientPlayNetworking.registerGlobalReceiver(QuestEditModePacket.TYPE, (payload, context) -> context.client().execute(() -> ClientPacketHandler.handle(payload, createClientContext())));
    }

    private static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(QuestRewardCollectPacket.TYPE, (payload, context) -> context.server().execute(() -> QuestRewardCollectPacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuestReadPacket.TYPE, (payload, context) -> context.server().execute(() -> QuestReadPacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuestEditSavePacket.TYPE, (payload, context) -> context.server().execute(() -> QuestEditSavePacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuestEditRemovePacket.TYPE, (payload, context) -> context.server().execute(() -> QuestEditRemovePacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ChapterEditSavePacket.TYPE, (payload, context) -> context.server().execute(() -> ChapterEditSavePacket.handle(payload, createServerContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ChapterEditRemovePacket.TYPE, (payload, context) -> context.server().execute(() -> ChapterEditRemovePacket.handle(payload, createServerContext(context.player()))));
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
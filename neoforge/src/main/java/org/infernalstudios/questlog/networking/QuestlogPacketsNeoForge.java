package org.infernalstudios.questlog.networking;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.network.packet.*;

public class QuestlogPacketsNeoForge {

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Questlog.MODID).versioned("1.0");

        // Client to Server
        registrar.playToServer(QuestRewardCollectPacket.TYPE, QuestRewardCollectPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestRewardCollectPacket.handle(payload, createServerContext(context.player())))
        );
        registrar.playToServer(QuestReadPacket.TYPE, QuestReadPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestReadPacket.handle(payload, createServerContext(context.player())))
        );

        // Server to Client
        registrar.playToClient(QuestSyncPacket.TYPE, QuestSyncPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestSyncPacket.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestDataPacket.TYPE, QuestDataPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestDataPacket.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestDefinitionPacket.TYPE, QuestDefinitionPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestDefinitionPacket.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestRemovePacket.TYPE, QuestRemovePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestRemovePacket.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestTriggeredPacket.TYPE, QuestTriggeredPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestTriggeredPacket.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestCompletedPacket.TYPE, QuestCompletedPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestCompletedPacket.handle(payload, createClientContext()))
        );
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
package org.infernalstudios.questlog.networking;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.ClientPacketHandler;
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
        registrar.playToServer(QuestEditSavePacket.TYPE, QuestEditSavePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestEditSavePacket.handle(payload, createServerContext(context.player())))
        );
        registrar.playToServer(QuestEditRemovePacket.TYPE, QuestEditRemovePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> QuestEditRemovePacket.handle(payload, createServerContext(context.player())))
        );
        registrar.playToServer(ChapterEditSavePacket.TYPE, ChapterEditSavePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ChapterEditSavePacket.handle(payload, createServerContext(context.player())))
        );
        registrar.playToServer(ChapterEditRemovePacket.TYPE, ChapterEditRemovePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ChapterEditRemovePacket.handle(payload, createServerContext(context.player())))
        );

        // Server to Client
        registrar.playToClient(QuestSyncPacket.TYPE, QuestSyncPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestDataPacket.TYPE, QuestDataPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestDefinitionPacket.TYPE, QuestDefinitionPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestRemovePacket.TYPE, QuestRemovePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestTriggeredPacket.TYPE, QuestTriggeredPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestCompletedPacket.TYPE, QuestCompletedPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestOpenPacket.TYPE, QuestOpenPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
        );
        registrar.playToClient(QuestEditModePacket.TYPE, QuestEditModePacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> ClientPacketHandler.handle(payload, createClientContext()))
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
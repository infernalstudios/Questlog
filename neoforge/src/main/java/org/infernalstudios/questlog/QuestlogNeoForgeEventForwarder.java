package org.infernalstudios.questlog;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class QuestlogNeoForgeEventForwarder {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        QuestlogEvents.onServerStart(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        QuestlogEvents.onPlayerSave((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        QuestlogEvents.onServerStop();
    }

    @SubscribeEvent
    public static void onServerPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        QuestlogEvents.onServerPlayerLogin((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        QuestlogEvents.registerCommands(event.getDispatcher());
    }

    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            QuestlogClientEvents.onClientTick();
        }

        @SubscribeEvent
        public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            QuestlogClientEvents.onClientPlayerLogin();
        }

        @SubscribeEvent
        public static void onClientPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            QuestlogClientEvents.onClientPlayerLogout();
        }
    }
}
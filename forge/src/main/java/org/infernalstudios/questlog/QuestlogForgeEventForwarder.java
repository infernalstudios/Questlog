package org.infernalstudios.questlog;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class QuestlogForgeEventForwarder {
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

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QuestlogClientEvents.onClientTick();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        QuestlogClientEvents.onClientPlayerLogin();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        QuestlogClientEvents.onClientPlayerLogout();
    }
}

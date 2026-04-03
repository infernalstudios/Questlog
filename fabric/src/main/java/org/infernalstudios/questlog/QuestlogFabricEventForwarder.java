package org.infernalstudios.questlog;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class QuestlogFabricEventForwarder {
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(QuestlogEvents::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> QuestlogEvents.onServerStop());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> QuestlogEvents.onServerPlayerLogin(handler.player));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> QuestlogEvents.registerCommands(dispatcher));
    }

    public static void initClient() {
        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> QuestlogClientEvents.onClientTick());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> QuestlogClientEvents.onClientPlayerLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QuestlogClientEvents.onClientPlayerLogout());
    }
}

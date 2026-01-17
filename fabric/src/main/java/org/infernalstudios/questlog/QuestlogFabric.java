package org.infernalstudios.questlog;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.infernalstudios.questlog.networking.QuestlogPacketsFabric;

public class QuestlogFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        QuestlogPacketsFabric.register();
        QuestlogFabricEventForwarder.init();
    }

    @Override
    public void onInitializeClient() {
        Questlog.initClient();
        KeyBindingHelper.registerKeyBinding(QuestlogClient.OPEN_SCREEN_KEY);
        QuestlogFabricEventForwarder.initClient();
    }
}

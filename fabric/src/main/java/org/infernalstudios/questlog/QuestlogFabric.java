package org.infernalstudios.questlog;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.infernalstudios.questlog.networking.QuestlogPacketsFabric;

public class QuestlogFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        QuestlogPacketsFabric.register();
        QuestlogFabricEventForwarder.init();
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(QuestlogClient.OPEN_SCREEN_KEY);
        QuestlogFabricEventForwarder.initClient();
    }
}

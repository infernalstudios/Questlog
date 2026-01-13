package org.infernalstudios.questlog;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.infernalstudios.questlog.networking.QuestlogPacketsNeoForge;

@Mod(Questlog.MODID)
public class QuestlogNeoForge {
    public QuestlogNeoForge(IEventBus modEventBus) {
        Questlog.init();

        modEventBus.register(QuestlogNeoForge.class);
        modEventBus.addListener(QuestlogPacketsNeoForge::register);
        NeoForge.EVENT_BUS.register(QuestlogNeoForgeEventForwarder.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Questlog.initClient();
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        Questlog.LOGGER.debug("Common setup complete");
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(QuestlogClient.OPEN_SCREEN_KEY);
    }
}
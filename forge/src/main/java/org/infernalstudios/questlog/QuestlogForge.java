package org.infernalstudios.questlog;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.infernalstudios.questlog.networking.QuestlogPacketsForge;

@Mod(Questlog.MODID)
public class QuestlogForge {
    public QuestlogForge() {
        Questlog.init();
        FMLJavaModLoadingContext.get().getModEventBus().register(QuestlogForge.class);
        MinecraftForge.EVENT_BUS.register(QuestlogForgeEventForwarder.class);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        Questlog.LOGGER.debug("Enqueueing network packet registration");
        event.enqueueWork(QuestlogPacketsForge::register);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(QuestlogClient.OPEN_SCREEN_KEY);
    }
}
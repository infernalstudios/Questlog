package org.infernalstudios.questlog;

import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.infernalstudios.questlog.networking.QuestlogPacketsNeoForge;

import static net.neoforged.api.distmarker.Dist.CLIENT;

@Mod(Questlog.MODID)
public class QuestlogNeoForge {
    public QuestlogNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Questlog.init();
        modEventBus.register(QuestlogNeoForge.class);
        modEventBus.addListener(QuestlogPacketsNeoForge::register);
        NeoForge.EVENT_BUS.register(QuestlogNeoForgeEventForwarder.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (container, parentScreen) -> AutoConfig.getConfigScreen(QuestlogConfig.class, parentScreen).get()
            );
        }
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
    @OnlyIn(CLIENT)
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(QuestlogClient.OPEN_SCREEN_KEY);
    }
}
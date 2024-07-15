package org.infernalstudios.questlog;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.network.NetworkHandler;
import org.lwjgl.glfw.GLFW;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Questlog.MODID)
public class Questlog {
  public static final String MODID = "questlog";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final KeyMapping OPEN_SCREEN_KEY = new KeyMapping("key.questlog.open", GLFW.GLFW_KEY_GRAVE_ACCENT, KeyMapping.CATEGORY_MISC);
  public static final GenericEventBus GENERIC_EVENT_BUS = new GenericEventBus(MinecraftForge.EVENT_BUS);

  public Questlog() {
    MinecraftForge.EVENT_BUS.register(QuestlogClientEvents.class);
    MinecraftForge.EVENT_BUS.register(QuestlogEvents.class);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(Questlog::onCommonSetup);
    FMLJavaModLoadingContext.get().getModEventBus().addListener((RegisterKeyMappingsEvent event) -> event.register(Questlog.OPEN_SCREEN_KEY));
  }
  
  @SubscribeEvent
  public static void onCommonSetup(FMLCommonSetupEvent event) {
    Questlog.LOGGER.debug("Enqueueing network packet registration");
    event.enqueueWork(NetworkHandler::register);
  }
}

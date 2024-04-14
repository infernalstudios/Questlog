package org.infernalstudios.questlog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.QuestManager;

public class QuestlogClientEvents {
  // These register and use 
  @SubscribeEvent
  public static void registerKeys(RegisterKeyMappingsEvent event) {
    event.register(Questlog.OPEN_SCREEN_KEY);
  }

  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;

    Minecraft minecraft = Minecraft.getInstance();

    if (Questlog.OPEN_SCREEN_KEY.consumeClick() && minecraft.isWindowActive()) {
      LocalPlayer player = minecraft.player;
      if (player != null) {
        minecraft.setScreen(new QuestlogScreen(minecraft.screen));
      }
    }
  }

  @SubscribeEvent
  public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
    QuestManager.getLocal();
  }

  @SubscribeEvent
  public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
    QuestManager.destroyLocal();
  }
}

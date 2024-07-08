package org.infernalstudios.questlog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestAddedToast;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestCompletedToast;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.QuestCompletedEvent;
import org.infernalstudios.questlog.event.QuestTriggeredEvent;

import java.util.ArrayList;
import java.util.List;

public class QuestlogClientEvents {
  // These register and use 
  @SubscribeEvent
  public static void registerKeys(RegisterKeyMappingsEvent event) {
    event.register(Questlog.OPEN_SCREEN_KEY);
  }

  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;

    if (QuestToastState.tickDelayForCheck >= 0) {
      QuestToastState.tickDelayForCheck--;
    }

    if (QuestToastState.tickDelayForCheck == 0) {
      QuestlogClientEvents.displayQueuedToasts();
    }

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


  private static class QuestToastState {
    // This allows for all network packets to be received before displaying toasts
    // This is necessary because the server may send an added quest before a completed quest,
    // and we want to display the completed quest first.
    public static int tickDelayForCheck = -1;
    public static List<QuestAddedToast> addedToasts = new ArrayList<>();
    public static List<QuestCompletedToast> completedToasts = new ArrayList<>();

    public static void resetCheckDelay() {
      QuestToastState.tickDelayForCheck = 10;
    }
  }

  // Called by QuestlogEvents.onQuestAdded
  public static void onQuestAdded(QuestTriggeredEvent event) {
    QuestToastState.resetCheckDelay();
    QuestToastState.addedToasts.add(new QuestAddedToast(event.getQuest().getDisplay()));
  }

  // Called by QuestlogEvents.onQuestCompleted
  public static void onQuestCompleted(QuestCompletedEvent event) {
    QuestToastState.resetCheckDelay();
    QuestToastState.completedToasts.add(new QuestCompletedToast(event.getQuest().getDisplay()));
  }

  private static void displayQueuedToasts() {
    ToastComponent toasts = Minecraft.getInstance().getToasts();
    if (!QuestToastState.completedToasts.isEmpty()) {
      for (QuestCompletedToast toast : QuestToastState.completedToasts) {
        toasts.addToast(toast);
      }
      QuestToastState.completedToasts.clear();
    }

    if (toasts.getToast(QuestCompletedToast.class, Toast.NO_TOKEN) != null) {
      QuestToastState.resetCheckDelay();
      return;
    }

    for (QuestAddedToast toast : QuestToastState.addedToasts) {
      toasts.addToast(toast);
    }
    QuestToastState.addedToasts.clear();
  }
}

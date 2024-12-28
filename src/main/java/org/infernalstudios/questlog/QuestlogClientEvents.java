package org.infernalstudios.questlog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.questlog.client.gui.components.QuestlogOpenButton;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestAddedToast;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestCompletedToast;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.config.QuestlogConfig.Button;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.event.QuestCompletedEvent;
import org.infernalstudios.questlog.event.QuestTriggeredEvent;

import java.util.ArrayList;
import java.util.List;

public class QuestlogClientEvents {
  @SubscribeEvent
  public static void onScreenInit(ScreenEvent.Init.Post event) {
    if (event.getScreen() instanceof InventoryScreen screen) {
      if (Button.enabled) {
        event.addListener(new QuestlogOpenButton(screen));
      }
    }
  }

  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.START) return;

    if (QuestToastState.tickDelayForCheck >= 0) {
      QuestToastState.tickDelayForCheck--;
    }

    if (QuestToastState.tickDelayForCheck == 0) {
      QuestlogClientEvents.displayQueuedPopups();
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
    public static List<Quest> queuedPopups = new ArrayList<>();

    public static void resetCheckDelay() {
      QuestToastState.tickDelayForCheck = 10;
    }
  }

  // Called by QuestlogEvents.onQuestAdded
  public static void onQuestAdded(QuestTriggeredEvent event) {
    // Popup only if singleplayer and not opened to lan, because the QuestDetails screen is a pauseable screen
    if (event.getQuest().getDisplay().shouldPopup() && Minecraft.getInstance().hasSingleplayerServer() && !Minecraft.getInstance().getSingleplayerServer().isPublished()) {
      QuestToastState.resetCheckDelay();
      QuestToastState.queuedPopups.add(event.getQuest());
    } else if (event.getQuest().getDisplay().shouldToastOnTrigger()) {
      QuestToastState.addedToasts.add(new QuestAddedToast(event.getQuest().getDisplay()));
    }

    SoundInstance triggeredSound = event.getQuest().getDisplay().getTriggeredSound();
    if (triggeredSound != null) {
        Minecraft.getInstance().getSoundManager().play(triggeredSound);
    }
  }

  // Called by QuestlogEvents.onQuestCompleted
  public static void onQuestCompleted(QuestCompletedEvent event) {
    if (event.getQuest().getDisplay().shouldToastOnComplete()) {
      QuestToastState.resetCheckDelay();
      QuestToastState.completedToasts.add(new QuestCompletedToast(event.getQuest().getDisplay()));
    }

    SoundInstance completedSound = event.getQuest().getDisplay().getCompletedSound();
    if (completedSound != null) {
      Minecraft.getInstance().getSoundManager().play(completedSound);
    }
  }

  private static void displayQueuedPopups() {
    if (QuestToastState.queuedPopups.isEmpty()) {
      return;
    }

    // Don't display popups if the player is viewing the QuestDetails screen
    // Make sure to reset the check delay so that the popup is displayed after the screen is closed
    if (Minecraft.getInstance().screen instanceof QuestDetails) {
      QuestToastState.resetCheckDelay();
      return;
    }

    // Don't display popups if the player is holding an item in their cursor
    if (Minecraft.getInstance().screen instanceof MenuAccess<?> screen && !screen.getMenu().getCarried().isEmpty()) {
      return;
    }

    Quest quest = QuestToastState.queuedPopups.get(0);
    QuestToastState.queuedPopups.remove(quest);

    Minecraft.getInstance().setScreen(new QuestDetails(Minecraft.getInstance().screen, quest));

    SoundInstance sound = quest.getDisplay().getTriggeredSound();
    if (sound != null) {
      Minecraft.getInstance().getSoundManager().play(sound);
    }

    QuestToastState.resetCheckDelay(); // Reset the check delay to ensure any following popups are displayed
  }

  private static void displayQueuedToasts() {
    if (Minecraft.getInstance().screen instanceof QuestDetails) {
      return;
    }

    ToastComponent toasts = Minecraft.getInstance().getToasts();
    if (!QuestToastState.completedToasts.isEmpty()) {
      for (QuestCompletedToast toast : QuestToastState.completedToasts) {
        toasts.addToast(toast);
      }
      QuestToastState.completedToasts.clear();
    }

    if (toasts.getToast(QuestCompletedToast.class, Toast.NO_TOKEN) != null) {
      QuestToastState.resetCheckDelay();
      return; // Return early so all completion toasts display before addition toasts
    }

    if (!QuestToastState.addedToasts.isEmpty()) {
      for (QuestAddedToast toast : QuestToastState.addedToasts) {
        toasts.addToast(toast);
      }
      QuestToastState.addedToasts.clear();
    }
  }
}

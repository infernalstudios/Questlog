package org.infernalstudios.questlog;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestAddedToast;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestCompletedToast;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.packet.QuestDefinitionPacket;

public class QuestlogClientEvents {
  public static void onClientTick() {
    if (QuestToastState.tickDelayForCheck >= 0) {
      QuestToastState.tickDelayForCheck--;
    }

    if (QuestToastState.tickDelayForCheck == 0) {
      QuestlogClientEvents.displayQueuedPopups();
      QuestlogClientEvents.displayQueuedToasts();
    }

    Minecraft minecraft = Minecraft.getInstance();

    if (QuestlogClient.OPEN_SCREEN_KEY.consumeClick() && minecraft.isWindowActive()) {
      LocalPlayer player = minecraft.player;
      if (player != null) {
        minecraft.setScreen(new QuestlogScreen(minecraft.screen));
      }
    }
  }

  public static void onClientPlayerLogin() {
    QuestlogClient.getLocal();
    QuestDefinitionPacket.handleDeferred();
  }

  public static void onClientPlayerLogout() {
    QuestlogClient.destroyLocal();
    Questlog.EVENTS.removeAllListeners();
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

  public static void onQuestTriggered(QuestEvent.Triggered event) {
    if (
      event.quest.getDisplay().shouldPopup() &&
      Minecraft.getInstance().hasSingleplayerServer() &&
      !Minecraft.getInstance().getSingleplayerServer().isPublished()
    ) {
      QuestToastState.resetCheckDelay();
      QuestToastState.queuedPopups.add(event.quest);
    } else if (event.quest.getDisplay().shouldToastOnTrigger()) {
      QuestToastState.resetCheckDelay();
      QuestToastState.addedToasts.add(new QuestAddedToast(event.quest.getDisplay()));
    }

    SoundEvent triggeredSound = event.quest.getDisplay().getTriggeredSound();
    if (triggeredSound != null) {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(triggeredSound, 1, 1));
    }
  }

  public static void onQuestCompleted(QuestEvent.Completed event) {
    if (event.quest.getDisplay().shouldToastOnComplete()) {
      QuestToastState.resetCheckDelay();
      QuestToastState.completedToasts.add(new QuestCompletedToast(event.quest.getDisplay()));
    }

    SoundEvent completedSound = event.quest.getDisplay().getCompletedSound();
    if (completedSound != null) {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(completedSound, 1, 1));
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

    SoundEvent sound = quest.getDisplay().getTriggeredSound();
    if (sound != null) {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, 1));
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

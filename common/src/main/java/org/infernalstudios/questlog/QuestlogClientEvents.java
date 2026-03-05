package org.infernalstudios.questlog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestAddedToast;
import org.infernalstudios.questlog.client.gui.components.toasts.QuestCompletedToast;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.packet.QuestDefinitionPacket;
import org.infernalstudios.questlog.network.packet.QuestSyncPacket;

import java.util.ArrayList;
import java.util.List;

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

        while (QuestlogClient.OPEN_SCREEN_KEY.consumeClick()) {
            if (minecraft.isWindowActive()) {
                LocalPlayer player = minecraft.player;
                if (player != null) {
                    minecraft.setScreen(new QuestlogScreen(minecraft.screen));
                }
            }
        }
    }

    public static void onClientPlayerLogin() {
        QuestlogClient.getLocal();
        QuestDefinitionPacket.handleDeferred();
        QuestSyncPacket.handleDeferred();
    }

    public static void onClientPlayerLogout() {
        QuestlogClient.destroyLocal();
        Questlog.EVENTS.removeAllListeners();
        QuestToastState.addedToasts.clear();
        QuestToastState.completedToasts.clear();
        QuestToastState.queuedPopups.clear();
    }

    public static void onQuestTriggered(QuestEvent.Triggered event) {
        if (
                event.quest.getDisplay().shouldShowPopupOnUnlock() &&
                        Minecraft.getInstance().hasSingleplayerServer() &&
                        !Minecraft.getInstance().getSingleplayerServer().isPublished()
        ) {
            QuestToastState.resetCheckDelay();
            QuestToastState.queuedPopups.add(event.quest);
        } else if (event.quest.getDisplay().shouldToastOnUnlock()) {
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

        for (Reward reward : event.quest.rewards) {
            if (reward.isAutoClaim()) {
                SoundEvent sound = reward.getDisplay().getClaimSound();
                if (sound != null) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1, 1));
                }
            }
        }
    }

    private static void displayQueuedPopups() {
        if (QuestToastState.queuedPopups.isEmpty()) {
            return;
        }

        if (Minecraft.getInstance().screen instanceof QuestDetails) {
            QuestToastState.resetCheckDelay();
            return;
        }

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

        QuestToastState.resetCheckDelay();
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
            return;
        }

        if (!QuestToastState.addedToasts.isEmpty()) {
            for (QuestAddedToast toast : QuestToastState.addedToasts) {
                toasts.addToast(toast);
            }
            QuestToastState.addedToasts.clear();
        }
    }

    private static class QuestToastState {
        public static int tickDelayForCheck = -1;
        public static List<QuestAddedToast> addedToasts = new ArrayList<>();
        public static List<QuestCompletedToast> completedToasts = new ArrayList<>();
        public static List<Quest> queuedPopups = new ArrayList<>();

        public static void resetCheckDelay() {
            QuestToastState.tickDelayForCheck = 10;
        }
    }
}

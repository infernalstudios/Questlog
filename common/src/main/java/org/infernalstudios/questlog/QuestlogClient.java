package org.infernalstudios.questlog;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.infernalstudios.questlog.core.QuestManager;
import org.lwjgl.glfw.GLFW;

public class QuestlogClient {
    public static final KeyMapping OPEN_SCREEN_KEY = new KeyMapping(
            "key.questlog.open",
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            KeyMapping.CATEGORY_MISC
    );


    private static QuestManager QUEST_MANAGER_INSTANCE;

    /**
     * This method is used to get the local instance of the QuestManager.
     * If the instance is null or the player is not the current Minecraft player, a new QuestManager instance is created.
     * If the instance is not local, an IllegalCallerException is thrown.
     *
     * @return Returns the local instance of the QuestManager.
     */
    public static QuestManager getLocal() {
        if (QUEST_MANAGER_INSTANCE != null && QUEST_MANAGER_INSTANCE.player == null) {
            QUEST_MANAGER_INSTANCE = null;
        }

        LocalPlayer mcPlayer = Minecraft.getInstance().player;
        if (QUEST_MANAGER_INSTANCE == null || !QUEST_MANAGER_INSTANCE.player.getUUID().equals(mcPlayer != null ? mcPlayer.getUUID() : null)) {
            if (mcPlayer == null) {
                throw new NullPointerException("QuestManager cannot be initialized, player is null\n");
            }

            QUEST_MANAGER_INSTANCE = new QuestManager(mcPlayer);
        } else if (QUEST_MANAGER_INSTANCE.player != mcPlayer) {
            QUEST_MANAGER_INSTANCE.player = mcPlayer;
        }

        if (!QUEST_MANAGER_INSTANCE.isClient()) {
            throw new IllegalCallerException("QuestManager is not local");
        }

        return QUEST_MANAGER_INSTANCE;
    }

    public static void destroyLocal() {
        QUEST_MANAGER_INSTANCE = null;
    }
}

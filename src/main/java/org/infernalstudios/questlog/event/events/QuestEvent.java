package org.infernalstudios.questlog.event.events;

import net.minecraft.world.entity.player.Player;
import org.infernalstudios.questlog.core.quests.Quest;

public class QuestEvent {
    public final Player player;
    public final Quest quest;
    public final boolean isServer;

    public QuestEvent(Player player, Quest quest, boolean isServer) {
        this.player = player;
        this.quest = quest;
        this.isServer = isServer;
    }

    public static class Completed extends QuestEvent {
        public Completed(Player player, Quest quest, boolean isServer) {
            super(player, quest, isServer);
        }
    }

    public static class Triggered extends QuestEvent {
        public Triggered(Player player, Quest quest, boolean isServer) {
            super(player, quest, isServer);
        }
    }

    public static class Read extends QuestEvent {
        public Read(Player player, Quest quest, boolean isServer) {
            super(player, quest, isServer);
        }
    }
}

package org.infernalstudios.questlog;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.infernalstudios.questlog.client.gui.components.scrollable.ScrollableInfo;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.rewards.ItemReward;
import net.minecraft.world.item.Items;
import org.infernalstudios.questlog.core.quests.rewards.Reward;

import java.util.ArrayList;
import java.util.List;

/** Verifies section height and input bounds without a renderer or a running game. */
public final class RewardOverviewVerification {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Quest quest = quest(1, 1);
        ScrollableInfo info = info(quest);
        check(info.getHeight() == 80, "Show both sections and their heading before completion");
        check(!info.mouseClicked(0, 55, 0), "Cannot choose rewards before completion");
        CompoundTag completed = new CompoundTag();
        completed.putInt("units", 1);
        quest.objectives.get(0).deserialize(completed);
        check(quest.isCompleted(), "Test quest is completed");
        check(info.getHeight() == 80, "Completion keeps objectives and rewards visible");
        check(!info.mouseClicked(0, 30, 0), "Clicking the rewards heading does not select a reward");
        check(!info.mouseClicked(0, -1, 0), "Reject negative reward coordinates");
        check(!info.mouseClicked(0, 500, 0), "Reject coordinates below the list");
        check(!info.mouseClicked(0, 55, 1), "Right click does not choose a reward");
        check(info(quest(0, 1)).getHeight() == 28, "Rewards-only quests do not duplicate their heading");
        check(info(quest(1, 0)).getHeight() == 28, "Objectives-only quests do not add a rewards heading");
        check(info(quest(0, 0)).getHeight() == 0, "Empty panels stay empty");
        check(info(quest(2, 3)).getHeight() == 164, "Multiple entries keep the section gap");
        Quest multiple = quest(1, 2);
        check(multiple.rewards.size() == 2, "Two separate rewards, without a choice container");
        check(((ItemReward) multiple.rewards.get(0)).getStack().is(Items.EMERALD)
                && ((ItemReward) multiple.rewards.get(0)).getStack().getCount() == 2, "Two emeralds");
        check(((ItemReward) multiple.rewards.get(1)).getStack().is(Items.DIAMOND)
                && ((ItemReward) multiple.rewards.get(1)).getStack().getCount() == 1, "One diamond");
        check(!multiple.isRewarded(), "Both rewards start unclaimed");
        check(info(multiple).getHeight() == 108, "Both item rewards are visible below the objective");
        check(!info(multiple).mouseClicked(0, 55, 0), "No premature collection or choice");
        System.out.println("Reward overview: 18 checks passed (headless, no game launched).");
    }

    private static ScrollableInfo info(Quest quest) {
        return new ScrollableInfo(new QuestDetails(null, quest), quest.getDisplay());
    }

    private static Quest quest(int objectiveCount, int rewardCount) {
        JsonObject definition = new JsonObject();
        definition.addProperty("title", "Test");
        definition.addProperty("description", "Overview");
        List<Objective> objectives = new ArrayList<>();
        for (int i = 0; i < objectiveCount; i++) {
            JsonObject objective = new JsonObject();
            objective.addProperty("name", "Test objective");
            objectives.add(new Objective(objective) {});
        }
        List<Reward> rewards = new ArrayList<>();
        for (int i = 0; i < rewardCount; i++) {
            JsonObject reward = new JsonObject();
            reward.addProperty("name", "Test reward");
            reward.addProperty("item", i % 2 == 0 ? "minecraft:emerald" : "minecraft:diamond");
            reward.addProperty("count", i % 2 == 0 ? 2 : 1);
            rewards.add(new ItemReward(reward));
        }
        QuestManager manager = new QuestManager(null) {
            @Override public boolean isClient() { return true; }
        };
        return new Quest(new QuestDisplayData(definition), new ArrayList<>(), objectives, new ArrayList<>(), rewards,
                new ResourceLocation("questlog", "test"), manager, false, false);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

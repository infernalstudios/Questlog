package org.infernalstudios.questlog;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.infernalstudios.questlog.client.gui.components.scrollable.ScrollableInfo;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.core.quests.rewards.ItemReward;
import org.infernalstudios.questlog.core.quests.rewards.Reward;

import java.util.ArrayList;
import java.util.List;

/** Verifies section height and input bounds without a renderer or a running game. */
public final class RewardOverviewVerification {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        check(!new QuestlogConfig().preferences.showRewardPreviews, "Existing installations keep previews disabled");
        Quest quest = quest(1, 1);
        ScrollableInfo defaultInfo = info(quest, false);
        ScrollableInfo previewInfo = info(quest, true);
        check(defaultInfo.getHeight() == 28, "Existing quests show only objectives by default");
        check(previewInfo.getHeight() == 90, "Opt-in preview adds rewards and their heading");
        check(info(quest(1, 1, true), false).getHeight() == 90,
                "Quest override can enable previews when the client default is off");
        check(info(quest(1, 1, false), true).getHeight() == 28,
                "Quest override can hide previews when the client default is on");
        check(!previewInfo.mouseClicked(0, 55, 0), "Cannot choose rewards before completion");
        CompoundTag completed = new CompoundTag();
        completed.putInt("units", 1);
        quest.objectives.get(0).deserialize(completed);
        check(quest.isCompleted(), "Test quest is completed");
        check(defaultInfo.getHeight() == 28, "Completion switches to the original rewards-only panel");
        check(previewInfo.getHeight() == 28, "Opt-in preview also keeps the original completed panel");
        check(!previewInfo.mouseClicked(0, -1, 0), "Reject negative reward coordinates");
        check(!previewInfo.mouseClicked(0, 500, 0), "Reject coordinates below the list");
        check(!previewInfo.mouseClicked(0, 5, 1), "Right click does not choose a reward");
        check(info(quest(0, 1), true).getHeight() == 28, "Rewards-only quests do not duplicate their heading");
        check(info(quest(1, 0), true).getHeight() == 28, "Objectives-only quests do not add a rewards heading");
        check(info(quest(0, 0), true).getHeight() == 0, "Empty panels stay empty");
        check(info(quest(2, 3), true).getHeight() == 174, "Multiple entries keep the section gap");
        check(info(quest(1, 2), true).getHeight() == 118, "Multiple reward rows fit below one objective");
        System.out.println("Reward overview: headless checks passed; in-game selection and visuals remain to test.");
    }

    private static ScrollableInfo info(Quest quest, boolean showRewardPreviews) {
        return new ScrollableInfo(new QuestDetails(null, quest), quest.getDisplay(),
                quest.getDisplay().shouldShowRewardPreviews(showRewardPreviews));
    }

    private static Quest quest(int objectiveCount, int rewardCount) {
        return quest(objectiveCount, rewardCount, null);
    }

    private static Quest quest(int objectiveCount, int rewardCount, Boolean rewardPreviewOverride) {
        JsonObject definition = new JsonObject();
        definition.addProperty("title", "Test");
        definition.addProperty("description", "Overview");
        if (rewardPreviewOverride != null) {
            definition.addProperty("show_rewards_before_completion", rewardPreviewOverride);
        }
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

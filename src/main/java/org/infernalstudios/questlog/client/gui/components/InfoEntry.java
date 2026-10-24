package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.core.quests.display.ObjectiveDisplayData;
import org.infernalstudios.questlog.core.quests.display.QuestDisplayData;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoEntry implements Renderable, GuiEventListener {
    public static final int INFO_ENTRY_HEIGHT = 28;
    private final QuestDetails questDetails;
    private final RewardDisplayData rewardData;
    private final ObjectiveDisplayData objectiveData;
    public int x, y;
    @Nullable
    private QuestDisplayData display;

    public InfoEntry(QuestDetails questDetails, @Nullable RewardDisplayData reward, int x, int y, @Nullable QuestDisplayData display) {
        this.questDetails = questDetails;
        this.rewardData = reward;
        this.objectiveData = null;
        this.x = x;
        this.y = y;
        this.display = display;
    }

    public InfoEntry(QuestDetails questDetails, @Nullable ObjectiveDisplayData objective, int x, int y) {
        this.questDetails = questDetails;
        this.rewardData = null;
        this.objectiveData = objective;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        boolean isReward = rewardData != null;
        Blittable icon = isReward ? rewardData.getIcon() : objectiveData.getIcon();
        Component name = isReward ? rewardData.getName() : objectiveData.getName();

        int indent = isReward ? rewardData.getIndentLevel() * 12 : objectiveData.getIndentLevel() * 12;
        int currentX = this.x + indent;

        if (icon != null) icon.blit(ps, currentX, this.y + 4);

        int textX = currentX + (icon != null ? 20 : 0);
        int maxWidth = questDetails.getDisplay().getRightPanelWidth() - 36 - 15 - (icon != null ? 20 : 0) - indent;

        Font font = Minecraft.getInstance().font;
        Component renderedName = name;
        boolean truncated = false;

        if (font.width(name) > maxWidth) {
            renderedName = Component.literal(font.plainSubstrByWidth(name.getString(), maxWidth - font.width("...")) + "...");
            truncated = true;
        }

        boolean isSubReward = isReward && rewardData.getReward() != null && rewardData.getReward().getContainer() != null;
        int nameY = isSubReward ? this.y + 9 : this.y + 2;

        ps.drawString(font, renderedName, textX, nameY, questDetails.getPalette().textColor(), false);

        if (truncated && mouseX >= textX && mouseX <= textX + font.width(renderedName) && mouseY >= nameY && mouseY <= nameY + font.lineHeight) {
            this.questDetails.pendingTooltip = name;
        }

        if (isReward) {
            this.drawRewardStatus(ps, textX);
        } else {
            this.drawObjectiveStatus(ps, textX);
        }
    }

    private void drawRewardStatus(GuiGraphics ps, int textX) {
        if (rewardData.getReward() != null && rewardData.getReward().getContainer() != null) {
            return;
        }
        Component status = rewardData.hasRewarded() ?
                (display != null ? display.getCollectedText() : Component.translatable("questlog.reward.collected")) :
                (display != null ? display.getUncollectedText() : Component.translatable("questlog.reward.uncollected"));

        ps.drawString(Minecraft.getInstance().font, status, textX, this.y + 13,
                rewardData.hasRewarded() ? questDetails.getPalette().completedTextColor() : questDetails.getPalette().progressTextColor(), false);
    }

    private void drawObjectiveStatus(GuiGraphics ps, int textX) {
        ps.drawString(Minecraft.getInstance().font, objectiveData.getProgress(), textX, this.y + 13,
                objectiveData.isCompleted() ? questDetails.getPalette().completedTextColor() : questDetails.getPalette().progressTextColor(), false);
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean var1) {
    }

    public boolean handleChoiceClick() {
        if (this.rewardData != null) {
            org.infernalstudios.questlog.core.quests.rewards.Reward reward = this.rewardData.getReward();
            if (reward != null && reward.getContainer() != null) {
                org.infernalstudios.questlog.core.quests.rewards.ChoiceReward choiceReward = reward.getContainer();
                if (!choiceReward.hasRewarded()) {
                    choiceReward.toggleChoice(reward);
                    net.minecraft.client.resources.sounds.SimpleSoundInstance sound =
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F
                            );
                    net.minecraft.client.Minecraft.getInstance().getSoundManager().play(sound);
                    return true;
                }
            }
        }
        return false;
    }
}
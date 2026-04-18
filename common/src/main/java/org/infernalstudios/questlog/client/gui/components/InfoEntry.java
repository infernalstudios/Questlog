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

        if (icon != null) icon.blit(ps, this.x, this.y + 4);

        int textX = this.x + (icon != null ? 20 : 0);
        int maxWidth = questDetails.getDisplay().getRightPanelWidth() - 36 - 15 - (icon != null ? 20 : 0);

        Font font = Minecraft.getInstance().font;
        Component renderedName = name;
        boolean truncated = false;

        if (font.width(name) > maxWidth) {
            renderedName = Component.literal(font.plainSubstrByWidth(name.getString(), maxWidth - font.width("...")) + "...");
            truncated = true;
        }

        ps.drawString(font, renderedName, textX, this.y + 2, questDetails.getPalette().textColor(), false);

        if (truncated && mouseX >= textX && mouseX <= textX + font.width(renderedName) && mouseY >= this.y + 2 && mouseY <= this.y + 2 + font.lineHeight) {
            this.questDetails.pendingTooltip = name;
        }

        if (isReward) {
            this.drawRewardStatus(ps, textX);
        } else {
            this.drawObjectiveStatus(ps, textX);
        }
    }

    private void drawRewardStatus(GuiGraphics ps, int textX) {
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
}
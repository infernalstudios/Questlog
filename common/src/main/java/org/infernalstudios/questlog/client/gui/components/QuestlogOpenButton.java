package org.infernalstudios.questlog.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogClientEvents;
import org.infernalstudios.questlog.client.gui.QuestlogGuiSet;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.mixin.client.AbstractContainerScreenAccessor;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.infernalstudios.questlog.util.texture.Texture;
import org.jetbrains.annotations.NotNull;

public class QuestlogOpenButton implements Renderable, GuiEventListener, NarratableEntry {

    public static final Texture TEXTURE = new Texture(
            new ResourceLocation(Questlog.MODID, "textures/gui/questlog_button.png"),
            64,
            64,
            96,
            96,
            256,
            256
    );

    public static final Texture DEFAULT_BADGE = new Texture(
            QuestlogGuiSet.DEFAULT.peripheralLoc,
            16, 16, 8, 12, 256, 256
    );

    private final InventoryScreen parent;

    public QuestlogOpenButton(InventoryScreen parent) {
        this.parent = parent;
    }

    private int getX() {
        return Questlog.getConfig().button.x + (Questlog.getConfig().button.relativeToInventory ? ((AbstractContainerScreenAccessor) this.parent).getLeftPos() : 0) - 18;
    }

    private int getY() {
        return Questlog.getConfig().button.y + (Questlog.getConfig().button.relativeToInventory ? ((AbstractContainerScreenAccessor) this.parent).getTopPos() : 0) - 18;
    }

    @Override
    public void render(@NotNull GuiGraphics ps, int mouseX, int mouseY, float partialTicks) {
        if (!Questlog.getConfig().button.enabled) return;

        TEXTURE.blit(ps, this.getX(), this.getY());

        Quest notifyQuest = null;

        if (Questlog.getConfig().button.showBadge) {
            notifyQuest = QuestlogClientEvents.mostRecentNotificationQuest;

            if (notifyQuest == null && Minecraft.getInstance().player != null) {
                QuestManager manager = QuestlogClient.getLocal();
                if (manager != null) {
                    for (Quest quest : manager.getAllQuests()) {
                        if (quest.isCompleted() && !quest.isRewarded()) {
                            notifyQuest = quest;
                            break;
                        }
                    }
                }
            }

            if (notifyQuest != null) {
                Blittable badgeToRender = notifyQuest.getDisplay().getBadge();
                if (badgeToRender == null) {
                    badgeToRender = DEFAULT_BADGE;
                }

                float bobOffset = 0;
                if (Questlog.getConfig().button.bobbingBadge) {
                    float time = (Minecraft.getInstance().level != null ?
                            Minecraft.getInstance().level.getGameTime() + partialTicks :
                            System.currentTimeMillis() / 50.0F);

                    bobOffset = (float) Math.sin(time * 0.2F) * 1.5F;
                }

                ps.pose().pushPose();
                ps.pose().translate(0, bobOffset, 0);

                badgeToRender.blit(ps, this.getX() + Questlog.getConfig().button.badgeX, this.getY() + Questlog.getConfig().button.badgeY);

                ps.pose().popPose();
            }
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            if (notifyQuest != null) {
                ps.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable("questlog.gui.open_notification", notifyQuest.getDisplay().getTitle()),
                        mouseX,
                        mouseY
                );
            } else {
                ps.renderTooltip(Minecraft.getInstance().font, Component.translatable("questlog.gui.open"), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Questlog.getConfig().button.enabled) return false;

        if (button == 0 && this.isMouseOver(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(new QuestlogScreen(Minecraft.getInstance().screen));
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!Questlog.getConfig().button.enabled) return false;

        return (
                mouseX >= this.getX() + 18 &&
                        mouseX < this.getX() + TEXTURE.width() - 18 &&
                        mouseY > this.getY() + 19 &&
                        mouseY <= this.getY() + TEXTURE.height() - 20
        );
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocused(boolean var1) {
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput var1) {
        // NO-OP
    }
}
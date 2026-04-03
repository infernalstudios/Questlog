package org.infernalstudios.questlog.core.quests.objectives.item;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemObtainObjective extends AbstractItemObjective {

    private int ticksUntilCheck = 0;

    public ItemObtainObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;

        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {

            int currentCount = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (this.test(stack)) {
                    currentCount += stack.getCount();
                }
            }

            if (currentCount > this.getUnits()) {
                this.setUnits(currentCount);
            }

            this.ticksUntilCheck = 10;
        }
    }
}
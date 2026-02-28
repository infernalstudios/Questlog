package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class ItemObtainObjective extends AbstractItemObjective {

    private int ticksUntilCheck = 0;

    public ItemObtainObjective(JsonObject definition) {
        super(definition);
    }

    @Override
    public void registerEventListeners(QuestlogEventBus bus) {
        super.registerEventListeners(bus);
        bus.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(QLPlayerEvent.Tick event) {
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
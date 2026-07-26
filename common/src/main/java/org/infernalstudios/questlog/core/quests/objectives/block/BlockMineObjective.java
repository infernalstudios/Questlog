package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.evandev.triggers.event.events.TriggerBlockEvent;
import org.infernalstudios.questlog.util.ItemMatcher;

import javax.annotation.Nullable;

public class BlockMineObjective extends AbstractBlockObjective {

    @Nullable
    private final ItemMatcher itemMatcher;

    public BlockMineObjective(JsonObject definition) {
        super(definition);
        if (definition.has("item") || definition.has("nbt")) {
            this.itemMatcher = ItemMatcher.fromDefinition(definition);
        } else {
            this.itemMatcher = null;
        }
    }

    private boolean testItem(ItemStack stack) {
        if (this.itemMatcher == null) {
            return true;
        }

        return this.itemMatcher.test(stack);
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        com.evandev.triggers.Triggers.EVENTS.addListener(this::onBlockDestroy);
    }

    private void onBlockDestroy(TriggerBlockEvent.Break event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.entity instanceof ServerPlayer player &&
                        this.getParent().manager.player.equals(player) &&
                        this.test(event.state) &&
                        this.testItem(player.getItemInHand(InteractionHand.MAIN_HAND))
        ) {
            this.setUnits(this.getUnits() + 1);
        }
    }
}

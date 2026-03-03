package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.infernalstudios.questlog.util.CachedRegistryPredicate;
import org.infernalstudios.questlog.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

public class BlockInteractObjective extends AbstractBlockObjective {

    @Nullable
    private final CachedRegistryPredicate<Item> item;

    public BlockInteractObjective(JsonObject definition) {
        super(definition);
        if (definition.has("item")) {
            this.item = CachedRegistryPredicate.item(JsonUtils.getString(definition, "item"));
        } else {
            this.item = null;
        }
    }

    private boolean testItem(ItemStack stack) {
        if (this.item == null) {
            return true;
        }
        return this.item.test(stack.getItem());
    }

    @Override
    public void registerEventListeners(QuestlogEventBus bus) {
        super.registerEventListeners(bus);
        bus.addListener(this::onBlockInteract);
    }

    private void onBlockInteract(QLBlockEvent.Interact event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.entity instanceof ServerPlayer player &&
                        this.getParent().manager.player.equals(player) &&
                        this.test(event.state) &&
                        this.testItem(event.itemStack)
        ) {
            this.setUnits(this.getUnits() + 1);
        }
    }
}
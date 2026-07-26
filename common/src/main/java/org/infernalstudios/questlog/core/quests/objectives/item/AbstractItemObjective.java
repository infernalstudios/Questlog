package org.infernalstudios.questlog.core.quests.objectives.item;

import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.ItemMatcher;

public abstract class AbstractItemObjective extends Objective {

    private final ItemMatcher itemMatcher;

    public AbstractItemObjective(JsonObject definition) {
        super(definition);
        this.itemMatcher = ItemMatcher.fromDefinition(definition);
    }

    protected boolean test(Item item) {
        return this.itemMatcher.test(item);
    }

    protected boolean test(ItemStack item) {
        return this.itemMatcher.test(item);
    }
}

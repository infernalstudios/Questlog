package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
//? if >=1.21
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

//? if <1.21
//import java.util.Map;

public class EnchantObjective extends Objective {

    @Nullable
    private final ResourceLocation enchantment;

    @Nullable
    private final ResourceLocation item;

    private final int level;

    public EnchantObjective(JsonObject definition) {
        super(definition);
        this.enchantment = definition.has("enchantment") ? Questlog.parseId(JsonUtils.getString(definition, "enchantment")) : null;
        this.level = JsonUtils.getOrDefault(definition, "level", 1);
        this.item = definition.has("item") ? Questlog.parseId(JsonUtils.getString(definition, "item")) : null;
    }

    //? if >=1.21 {
    private boolean areEnchantmentsEqual(Holder<Enchantment> enchantmentHolder) {
        if (this.enchantment == null) return true;
        return enchantmentHolder.is(this.enchantment);
    }
    //?} else {
    /*private boolean areEnchantmentsEqual(Enchantment enchantment) {
        return this.enchantment == null || this.enchantment.equals(BuiltInRegistries.ENCHANTMENT.getKey(enchantment));
    }
    *///?}

    private boolean areItemsEqual(Item item) {
        return this.item == null || this.item.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        this.addListener(TriggerPlayerEvent.Enchant.class, this::onItemEnchanted);
    }

    private void onItemEnchanted(TriggerPlayerEvent.Enchant event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (
                event.player instanceof ServerPlayer player &&
                        player.equals(this.getParent().manager.player) &&
                        areItemsEqual(event.item.getItem())
        ) {
            //? if >=1.21 {
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(event.item).entrySet()) {
                if (entry.getIntValue() >= this.level && areEnchantmentsEqual(entry.getKey())) {
            //?} else {
            /*for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(event.item).entrySet()) {
                if (entry.getValue() >= this.level && areEnchantmentsEqual(entry.getKey())) {
            *///?}
                    this.setUnits(this.getUnits() + 1);
                    return;
                }
            }
        }
    }
}
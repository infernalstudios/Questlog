package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;

public class EnchantObjective extends Objective {

  @Nullable
  private final ResourceLocation enchantment;

  @Nullable
  private final ResourceLocation item;

  private final int level;

  public EnchantObjective(JsonObject definition) {
    super(definition);
    this.enchantment = definition.has("enchantment") ? new ResourceLocation(JsonUtils.getString(definition, "enchantment")) : null;
    this.level = JsonUtils.getOrDefault(definition, "level", 1);
    this.item = definition.has("item") ? new ResourceLocation(JsonUtils.getString(definition, "item")) : null;
  }

  private boolean areEnchantmentsEqual(Enchantment enchantment) {
    return this.enchantment == null || this.enchantment.equals(BuiltInRegistries.ENCHANTMENT.getKey(enchantment));
  }

  private boolean areItemsEqual(Item item) {
    return this.item == null || this.item.equals(BuiltInRegistries.ITEM.getKey(item));
  }

  @Override
  public void registerEventListeners(QuestlogEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onItemEnchanted);
  }

  private void onItemEnchanted(QLPlayerEvent.Enchant event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.player instanceof ServerPlayer player &&
      player.equals(this.getParent().manager.player) &&
      areItemsEqual(event.item.getItem())
    ) {
      for (Map.Entry<Enchantment, Integer> enchantment : EnchantmentHelper.getEnchantments(event.item).entrySet()) {
        if (enchantment.getValue() >= this.level && areEnchantmentsEqual(enchantment.getKey())) {
          this.setUnits(this.getUnits() + 1);
          return;
        }
      }
    }
  }
}

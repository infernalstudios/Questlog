package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.event.ItemEnchantedEvent;
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
    return this.enchantment == null || this.enchantment.equals(ForgeRegistries.ENCHANTMENTS.getKey(enchantment));
  }

  private boolean areItemsEqual(Item item) {
    return this.item == null || this.item.equals(ForgeRegistries.ITEMS.getKey(item));
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onItemEnchanted);
  }

  private void onItemEnchanted(ItemEnchantedEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.getEntity() instanceof ServerPlayer &&
      event.getEntity().equals(this.getParent().manager.player) &&
      areItemsEqual(event.getEnchantedStack().getItem())
    ) {
      for (Map.Entry<Enchantment, Integer> enchantment : event.getEnchantedStack().getAllEnchantments().entrySet()) {
        if (enchantment.getValue() >= this.level && areEnchantmentsEqual(enchantment.getKey())) {
          this.setUnits(this.getUnits() + 1);
          return;
        }
      }
    }
  }
}

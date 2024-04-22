package org.infernalstudios.questlog.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ItemEnchantedEvent extends PlayerEvent {
  private final ItemStack enchantedStack;
  private final int enchantSlot;

  public ItemEnchantedEvent(Player player, ItemStack enchantedStack, int enchantSlot) {
    super(player);
    this.enchantedStack = enchantedStack;
    this.enchantSlot = enchantSlot;
  }

  public ItemStack getEnchantedStack() {
    return this.enchantedStack;
  }

  public int getEnchantSlot() {
    return this.enchantSlot;
  }
}

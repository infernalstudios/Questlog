package org.infernalstudios.questlog.event.events;

import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QLPlayerEvent extends QLEvent {
  public final Player player;

  public QLPlayerEvent(Player player) {
    this.player = player;
  }

  public static class Enchant extends QLPlayerEvent {
    public final ItemStack item;
    public final int slot;

    public Enchant(Player player, ItemStack item, int slot) {
      super(player);

      this.item = item;
      this.slot = slot;
    }
  }

  public static class Tick extends QLPlayerEvent {
    public Tick(Player player) {
      super(player);
    }
  }

  public static class StatAward extends QLPlayerEvent {
    public final Stat<?> stat;
    public final int amount;

    public StatAward(Player player, Stat<?> stat, int amount) {
      super(player);
      this.stat = stat;
      this.amount = amount;
    }
  }

  public static class Craft extends QLPlayerEvent {
    public final ItemStack outputItem;

    public Craft(Player player, ItemStack outputItem) {
      super(player);
      this.outputItem = outputItem;
    }
  }
}

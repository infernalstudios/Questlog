package org.infernalstudios.questlog.event;

import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class StatAwardEvent extends PlayerEvent {
  private final Stat<?> stat;
  private final int amount;

  public StatAwardEvent(Player player, Stat<?> stat, int amount) {
    super(player);
    this.stat = stat;
    this.amount = amount;
  }

  public Stat<?> getStat() {
    return this.stat;
  }

  public int getAmount() {
    return this.amount;
  }
}

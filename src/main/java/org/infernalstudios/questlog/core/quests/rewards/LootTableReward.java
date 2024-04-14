package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.Util;

import java.util.List;

public class LootTableReward extends Reward {
  private final ResourceLocation lootTable;

  public LootTableReward(JsonObject definition) {
    super(definition);

    this.lootTable = new ResourceLocation(definition.get("loot_table").getAsString());
  }

  @Override
  public void applyReward(ServerPlayer player) {
    LootTables tables = player.getServer().getLootTables();
    LootTable table = tables.get(this.lootTable);

    if (table == LootTable.EMPTY) {
      Questlog.LOGGER.error("Loot table not found: {}", this.lootTable);
    }

    List<ItemStack> stacks = table.getRandomItems(
      new LootContext.Builder(player.getLevel())
        .withRandom(player.getRandom())
        .withParameter(LootContextParams.ORIGIN, player.position())
        .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
        .create(LootContextParamSets.EMPTY)
    );

    for (ItemStack stack : stacks) {
      Util.giveToPlayer(player, stack);
    }

    super.applyReward(player);
  }
}

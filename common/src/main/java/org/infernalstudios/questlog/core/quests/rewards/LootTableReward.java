package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;

import java.util.List;

public class LootTableReward extends Reward {

    private final ResourceLocation lootTable;

    public LootTableReward(JsonObject definition) {
        super(definition);
        this.lootTable = ResourceLocation.parse(JsonUtils.getString(definition, "loot_table"));
    }

    @Override
    public void applyReward(ServerPlayer player) {
        LootTable table = player.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, this.lootTable));

        if (table == LootTable.EMPTY) {
            Questlog.LOGGER.error("Loot table not found: {}", this.lootTable);
        }

        List<ItemStack> stacks = table.getRandomItems(
                new LootParams.Builder(player.serverLevel())
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                        .create(LootContextParamSets.EMPTY)
        );

        for (ItemStack stack : stacks) {
            Util.giveToPlayer(player, stack);
        }

        super.applyReward(player);
    }
}
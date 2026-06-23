package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.CachedValue;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;
import org.jetbrains.annotations.Nullable;

public class ItemReward extends Reward {

    private final CachedValue<ItemStack> stack;

    public ItemReward(JsonObject definition) {
        super(definition);
        this.stack = new CachedValue<>(() -> {
            Quest parent = getParent();
            HolderLookup.Provider registries = null;
            if (parent != null && parent.manager != null && parent.manager.player != null) {
                registries = parent.manager.player.level().registryAccess();
            }
            ItemStack parsed = parseItemStack(definition.get("item"), registries);
            if (!parsed.isEmpty()) {
                parsed.setCount(JsonUtils.getOrDefault(definition, "count", 1));
            }
            return parsed;
        });
    }

    public ItemStack getStack() {
        return this.stack.get();
    }

    @Override
    public void applyReward(ServerPlayer player) {
        ItemStack itemStack = this.stack.get();
        if (!itemStack.isEmpty()) {
            Util.giveToPlayer(player, itemStack.copy());
        }
        super.applyReward(player);
    }

    public static ItemStack parseItemStack(JsonElement element, @Nullable HolderLookup.Provider registries) {
        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String itemStr = element.getAsString();
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemStr));
            return new ItemStack(item);
        }
        if (element.isJsonObject()) {
            HolderLookup.Provider opsRegistries = registries != null ? registries : RegistryAccess.EMPTY;
            try {
                return ItemStack.CODEC.parse(
                        RegistryOps.create(JsonOps.INSTANCE, opsRegistries),
                        element
                ).result().orElse(ItemStack.EMPTY);
            } catch (Exception e) {
                org.infernalstudios.questlog.Questlog.LOGGER.error("Failed to parse ItemStack from JSON", e);
            }
        }
        return ItemStack.EMPTY;
    }
}

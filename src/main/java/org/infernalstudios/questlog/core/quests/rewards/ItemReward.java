package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
//? if <1.21 {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
*///?}
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.util.CachedValue;
import org.infernalstudios.questlog.util.ItemMatcher;
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
            //? if >=1.21 {
            ItemStack parsed = parseItemStack(withTopLevelComponents(definition.get("item"), definition.get(ItemMatcher.COMPONENTS_KEY)), registries);
            //?} else {
            /*ItemStack parsed = parseItemStack(definition.get("item"), registries);
            applyTopLevelNbt(parsed, definition.get(ItemMatcher.COMPONENTS_KEY));
            *///?}
            if (!parsed.isEmpty()) {
                parsed.setCount(JsonUtils.getOrDefault(definition, "count", 1));
            }
            return parsed;
        });
    }

    public static ItemStack parseItemStack(JsonElement element, @Nullable HolderLookup.Provider registries) {
        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String itemStr = element.getAsString();
            Item item = BuiltInRegistries.ITEM.get(Questlog.parseId(itemStr));
            return new ItemStack(item);
        }
        if (element.isJsonObject()) {
            HolderLookup.Provider opsRegistries = registries;
            if (opsRegistries == null || opsRegistries == RegistryAccess.EMPTY) {
                opsRegistries = Util.getFallbackRegistries();
                if (opsRegistries == null) {
                    opsRegistries = RegistryAccess.EMPTY;
                }
            }
            try {
                ItemStack stack = ItemStack.CODEC.parse(
                        RegistryOps.create(JsonOps.INSTANCE, opsRegistries),
                        element
                ).result().orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    return stack;
                }
            } catch (Exception e) {
                org.infernalstudios.questlog.Questlog.LOGGER.error("Failed to parse ItemStack from JSON", e);
            }

            JsonObject obj = element.getAsJsonObject();
            String idStr = obj.has("id") && obj.get("id").isJsonPrimitive() ? obj.get("id").getAsString() : (obj.has("item") && obj.get("item").isJsonPrimitive() ? obj.get("item").getAsString() : null);
            if (idStr != null) {
                Item item = BuiltInRegistries.ITEM.get(Questlog.parseId(idStr));
                if (item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    //? if >=1.21 {
    @Nullable
    private static JsonElement withTopLevelComponents(@Nullable JsonElement item, @Nullable JsonElement components) {
        if (item == null || components == null || !components.isJsonObject() || components.getAsJsonObject().size() == 0) {
            return item;
        }
        JsonObject merged;
        if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
            merged = new JsonObject();
            merged.addProperty("id", item.getAsString());
        } else if (item.isJsonObject()) {
            merged = item.getAsJsonObject().deepCopy();
            if (!merged.has("id") && merged.has("item") && merged.get("item").isJsonPrimitive()) {
                merged.add("id", merged.remove("item"));
            }
        } else {
            return item;
        }
        JsonObject mergedComponents = merged.has(ItemMatcher.COMPONENTS_KEY) && merged.get(ItemMatcher.COMPONENTS_KEY).isJsonObject()
                ? merged.getAsJsonObject(ItemMatcher.COMPONENTS_KEY)
                : new JsonObject();
        for (var entry : components.getAsJsonObject().entrySet()) {
            mergedComponents.add(entry.getKey(), entry.getValue().deepCopy());
        }
        merged.add(ItemMatcher.COMPONENTS_KEY, mergedComponents);
        return merged;
    }
    //?} else {
    /*
    private static void applyTopLevelNbt(ItemStack stack, @Nullable JsonElement nbt) {
        if (stack.isEmpty() || nbt == null || nbt.isJsonNull()) {
            return;
        }
        try {
            CompoundTag tag;
            if (nbt.isJsonPrimitive()) {
                tag = TagParser.parseTag(nbt.getAsString());
            } else if (nbt.isJsonObject()) {
                Tag converted = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, nbt);
                if (!(converted instanceof CompoundTag compound)) {
                    return;
                }
                tag = compound;
            } else {
                return;
            }
            stack.getOrCreateTag().merge(tag);
        } catch (Exception e) {
            Questlog.LOGGER.error("Failed to parse item reward NBT: {}", nbt, e);
        }
    }
    *///?}

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
}

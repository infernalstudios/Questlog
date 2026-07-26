package org.infernalstudios.questlog.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.Nullable;

public class ItemMatcher {

    @Nullable
    private final CachedRegistryPredicate<Item> itemPredicate;
    @Nullable
    private final JsonElement nbtJson;

    private NbtPredicate cachedNbtPredicate;
    private boolean failedParsingNbt = false;

    public ItemMatcher(@Nullable JsonElement itemElement, @Nullable JsonElement nbtElement) {
        if (itemElement != null && !itemElement.isJsonNull()) {
            if (itemElement.isJsonPrimitive() && itemElement.getAsJsonPrimitive().isString()) {
                this.itemPredicate = CachedRegistryPredicate.item(itemElement.getAsString());
                this.nbtJson = nbtElement;
            } else if (itemElement.isJsonObject()) {
                JsonObject itemObj = itemElement.getAsJsonObject();
                if (itemObj.has("id")) {
                    this.itemPredicate = CachedRegistryPredicate.item(JsonUtils.getString(itemObj, "id"));
                } else if (itemObj.has("item")) {
                    this.itemPredicate = CachedRegistryPredicate.item(JsonUtils.getString(itemObj, "item"));
                } else {
                    this.itemPredicate = null;
                }

                if (itemObj.has("nbt")) {
                    this.nbtJson = itemObj.get("nbt");
                } else {
                    this.nbtJson = nbtElement;
                }
            } else {
                this.itemPredicate = null;
                this.nbtJson = nbtElement;
            }
        } else {
            this.itemPredicate = null;
            this.nbtJson = nbtElement;
        }
    }

    public static ItemMatcher fromDefinition(JsonObject definition) {
        JsonElement itemElement = definition.get("item");
        JsonElement nbtElement = definition.get("nbt");
        return new ItemMatcher(itemElement, nbtElement);
    }

    public boolean test(Item item) {
        return this.itemPredicate == null || this.itemPredicate.test(item);
    }

    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.itemPredicate != null && !this.itemPredicate.test(stack.getItem())) {
            return false;
        }
        if (this.nbtJson != null && !this.nbtJson.isJsonNull()) {
            NbtPredicate predicate = getNbtPredicate();
            if (predicate != null) {
                return predicate.matches(stack);
            }
        }
        return true;
    }

    @Nullable
    private NbtPredicate getNbtPredicate() {
        if (this.cachedNbtPredicate == null && !this.failedParsingNbt) {
            try {
                this.cachedNbtPredicate = NbtPredicate.fromJson(this.nbtJson);
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse NBT predicate from JSON: {}", this.nbtJson, e);
                this.failedParsingNbt = true;
            }
        }
        return this.cachedNbtPredicate;
    }
}

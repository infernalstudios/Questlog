package org.infernalstudios.questlog.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.Nullable;

public class ItemMatcher {

    @Nullable
    private final CachedRegistryPredicate<Item> itemPredicate;
    @Nullable
    private final JsonElement componentsJson;

    private DataComponentPredicate cachedComponentsPredicate;
    private boolean failedParsingComponents = false;

    public ItemMatcher(@Nullable JsonElement itemElement, @Nullable JsonElement componentsElement) {
        if (itemElement != null && !itemElement.isJsonNull()) {
            if (itemElement.isJsonPrimitive() && itemElement.getAsJsonPrimitive().isString()) {
                this.itemPredicate = CachedRegistryPredicate.item(itemElement.getAsString());
                this.componentsJson = componentsElement;
            } else if (itemElement.isJsonObject()) {
                JsonObject itemObj = itemElement.getAsJsonObject();
                if (itemObj.has("id")) {
                    this.itemPredicate = CachedRegistryPredicate.item(JsonUtils.getString(itemObj, "id"));
                } else if (itemObj.has("item")) {
                    this.itemPredicate = CachedRegistryPredicate.item(JsonUtils.getString(itemObj, "item"));
                } else {
                    this.itemPredicate = null;
                }

                if (itemObj.has("components")) {
                    this.componentsJson = itemObj.get("components");
                } else {
                    this.componentsJson = componentsElement;
                }
            } else {
                this.itemPredicate = null;
                this.componentsJson = componentsElement;
            }
        } else {
            this.itemPredicate = null;
            this.componentsJson = componentsElement;
        }
    }

    public static ItemMatcher fromDefinition(JsonObject definition) {
        JsonElement itemElement = definition.get("item");
        JsonElement componentsElement = definition.get("components");
        return new ItemMatcher(itemElement, componentsElement);
    }

    public boolean test(Item item) {
        return this.itemPredicate == null || this.itemPredicate.test(item);
    }

    public boolean test(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.itemPredicate != null && !this.itemPredicate.test(stack.getItem())) {
            return false;
        }
        if (this.componentsJson != null && !this.componentsJson.isJsonNull()) {
            DataComponentPredicate predicate = getComponentsPredicate(registries);
            if (predicate != null) {
                return predicate.test(stack);
            }
        }
        return true;
    }

    @Nullable
    private DataComponentPredicate getComponentsPredicate(@Nullable HolderLookup.Provider registries) {
        if (this.cachedComponentsPredicate == null && !this.failedParsingComponents && this.componentsJson != null) {
            HolderLookup.Provider opsRegistries = registries;
            if (opsRegistries == null || opsRegistries == RegistryAccess.EMPTY) {
                if (Minecraft.getInstance().level != null) {
                    opsRegistries = Minecraft.getInstance().level.registryAccess();
                }
            }
            if (opsRegistries == null || opsRegistries == RegistryAccess.EMPTY) {
                return null;
            }
            try {
                var result = DataComponentPredicate.CODEC.parse(
                        RegistryOps.create(JsonOps.INSTANCE, opsRegistries),
                        this.componentsJson
                ).result();
                if (result.isPresent()) {
                    this.cachedComponentsPredicate = result.get();
                } else {
                    Questlog.LOGGER.error("Failed to parse DataComponentPredicate from JSON: {}", this.componentsJson);
                    this.failedParsingComponents = true;
                }
            } catch (Exception e) {
                Questlog.LOGGER.error("Failed to parse DataComponentPredicate from JSON", e);
                this.failedParsingComponents = true;
            }
        }
        return this.cachedComponentsPredicate;
    }
}

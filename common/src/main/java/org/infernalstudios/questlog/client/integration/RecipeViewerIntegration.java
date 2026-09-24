package org.infernalstudios.questlog.client.integration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.platform.Services;

/** Opens the output recipes for an item in an installed recipe viewer. */
public final class RecipeViewerIntegration {
    private RecipeViewerIntegration() {
    }

    public static boolean openRecipes(ResourceLocation itemId) {
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) return false;
        ItemStack stack = BuiltInRegistries.ITEM.get(itemId).getDefaultInstance();
        if (stack.isEmpty()) return false;

        if (Services.PLATFORM.isModLoaded("emi")) {
            EmiRecipeViewer.open(stack);
            return true;
        }
        if (Services.PLATFORM.isModLoaded("jei")) {
            return JeiRecipeViewer.open(stack);
        }
        return false;
    }
}

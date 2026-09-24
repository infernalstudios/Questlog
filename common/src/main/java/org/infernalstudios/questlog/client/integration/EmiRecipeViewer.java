package org.infernalstudios.questlog.client.integration;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

/** Isolated so the EMI API is never loaded when EMI is absent. */
final class EmiRecipeViewer {
    private EmiRecipeViewer() {
    }

    static void open(ItemStack stack) {
        EmiApi.displayRecipes(EmiStack.of(stack));
    }
}

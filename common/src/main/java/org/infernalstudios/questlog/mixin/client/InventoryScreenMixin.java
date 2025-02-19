package org.infernalstudios.questlog.mixin.client;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.infernalstudios.questlog.client.gui.components.QuestlogOpenButton;
import org.infernalstudios.questlog.config.QuestlogConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
  public InventoryScreenMixin(InventoryMenu $$0, Inventory $$1, Component $$2) {
    super($$0, $$1, $$2);
    throw new UnsupportedOperationException("Mixin constructor");
  }

  @Inject(method = "init", at = @At("TAIL"))
  private void injectQuestlogButton(CallbackInfo info) {
    if (QuestlogConfig.Button.enabled) {
      InventoryScreen self = (InventoryScreen) (Object) this;
      this.addRenderableWidget(new QuestlogOpenButton(self));
    }
  }
}

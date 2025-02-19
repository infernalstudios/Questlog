package org.infernalstudios.questlog;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.infernalstudios.questlog.event.events.QLEntityEvent;

public class QuestlogFabricEventForwarder {
  public static void init() {
    ServerLifecycleEvents.SERVER_STARTING.register(QuestlogEvents::onServerStart);
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> QuestlogEvents.onServerStop());
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> QuestlogEvents.onServerPlayerLogin(handler.player));

    // Event forwarding for objectives (events not seen here are not supported by fabric api, and are posted using mixins)
    PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> Questlog.EVENTS.post(new QLBlockEvent.Break(state, pos, player)));

    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      if (!player.isSpectator()) {
        Questlog.EVENTS.post(new QLBlockEvent.Place(world.getBlockState(hitResult.getBlockPos()), hitResult.getBlockPos(), player));
      }
      return InteractionResult.PASS;
    });

    ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> Questlog.EVENTS.post(new QLEntityEvent.Death(entity, source)));

    UseItemCallback.EVENT.register((player, world, hand) -> {
      if (!player.isSpectator()) {
        Questlog.EVENTS.post(new QLEntityEvent.UseItem(player, player.getItemInHand(hand)));
      }
      return InteractionResultHolder.pass(ItemStack.EMPTY);
    });

    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
      @Override
      public ResourceLocation getFabricId() {
        return new ResourceLocation(Questlog.MODID, "questlog_data");
      }

      @Override
      public void onResourceManagerReload(ResourceManager resourceManager) {
        QuestlogEvents.onDataPackReload(resourceManager);
      }
    });
  }

  public static void initClient() {
    ClientTickEvents.START_CLIENT_TICK.register(minecraft -> QuestlogClientEvents.onClientTick());
    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> QuestlogClientEvents.onClientPlayerLogin());
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> QuestlogClientEvents.onClientPlayerLogout());
  }
}

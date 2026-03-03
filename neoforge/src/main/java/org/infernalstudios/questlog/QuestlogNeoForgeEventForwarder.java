package org.infernalstudios.questlog;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.infernalstudios.questlog.core.DefinitionUtil;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class QuestlogNeoForgeEventForwarder {
    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        QuestlogEvents.onServerStart(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        QuestlogEvents.onPlayerSave((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        QuestlogEvents.onServerStop();
    }

    @SubscribeEvent
    public static void onServerPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        QuestlogEvents.onServerPlayerLogin((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new DefinitionUtil.QuestDefinitionReloadListener());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        QuestlogEvents.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(ClientTickEvent.Post event) {
        QuestlogClientEvents.onClientTick();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        QuestlogClientEvents.onClientPlayerLogin();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        QuestlogClientEvents.onClientPlayerLogout();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Questlog.EVENTS.post(new QLBlockEvent.Break(event.getState(), event.getPos(), event.getPlayer()));
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            Questlog.EVENTS.post(new QLBlockEvent.Place(event.getState(), event.getPos(), entity));
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide) {
            Questlog.EVENTS.post(new QLBlockEvent.Interact(
                    event.getLevel().getBlockState(event.getPos()),
                    event.getPos(),
                    event.getEntity(),
                    event.getItemStack()
            ));
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            Questlog.EVENTS.post(new QLBlockEvent.FarmlandTrample(event.getState(), event.getPos(), entity));
        }
    }

    @SubscribeEvent
    public static void onEntityBreed(BabyEntitySpawnEvent event) {
        Questlog.EVENTS.post(new QLEntityEvent.Breed(event.getChild(), event.getParentA(), event.getParentB(), event.getCausedByPlayer()));
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Questlog.EVENTS.post(new QLEntityEvent.Death(event.getEntity(), event.getSource()));
    }

    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent.Finish event) {
        Questlog.EVENTS.post(new QLEntityEvent.UseItem(event.getEntity(), event.getItem()));
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        Questlog.EVENTS.post(new QLEntityEvent.TossItem(event.getPlayer(), event.getEntity().getItem()));
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        Questlog.EVENTS.post(new QLEntityEvent.PickupItem(event.getPlayer(), event.getOriginalStack()));
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        Questlog.EVENTS.post(new QLEntityEvent.EffectAdded(event.getEntity(), event.getEffectInstance()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Questlog.EVENTS.post(new QLPlayerEvent.Tick(event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerCraft(PlayerEvent.ItemCraftedEvent event) {
        Questlog.EVENTS.post(new QLPlayerEvent.Craft(event.getEntity(), event.getCrafting()));
    }

    @SubscribeEvent
    public static void onPlayerSmelt(PlayerEvent.ItemSmeltedEvent event) {
        Questlog.EVENTS.post(new QLPlayerEvent.Craft(event.getEntity(), event.getSmelting()));
    }
}
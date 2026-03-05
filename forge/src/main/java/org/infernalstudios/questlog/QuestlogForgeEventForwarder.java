package org.infernalstudios.questlog;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.questlog.event.events.QLBlockEvent;
import org.infernalstudios.questlog.event.events.QLEntityEvent;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;

public class QuestlogForgeEventForwarder {
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
    public static void registerCommands(RegisterCommandsEvent event) {
        QuestlogEvents.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QuestlogClientEvents.onClientTick();
        }
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
            Questlog.EVENTS.post(new QLBlockEvent.Interact(event.getLevel().getBlockState(event.getPos()), event.getPos(), event.getEntity(), event.getItemStack()));
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
    public static void onItemPickup(EntityItemPickupEvent event) {
        Questlog.EVENTS.post(new QLEntityEvent.PickupItem(event.getEntity(), event.getItem().getItem()));
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        Questlog.EVENTS.post(new QLEntityEvent.EffectAdded(event.getEntity(), event.getEffectInstance()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Questlog.EVENTS.post(new QLPlayerEvent.Tick(event.player));
        }
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
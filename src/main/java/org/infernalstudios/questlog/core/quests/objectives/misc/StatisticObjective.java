package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.GenericEventBus;
import org.infernalstudios.questlog.event.StatAwardEvent;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StatisticObjective extends Objective {
  private final ResourceLocation stat;
  private int statAtStart = 0;
  private boolean trackSinceStart = true;

  public StatisticObjective(JsonObject definition) {
    super(definition);
    this.stat = STAT_MAP.get(new ResourceLocation(JsonUtils.getString(definition, "stat")));
    if (definition.has("trackSinceStart")) {
      this.trackSinceStart = JsonUtils.getBoolean(definition, "trackSinceStart");
    }
  }

  @Override
  protected void registerEventListeners(GenericEventBus bus) {
    super.registerEventListeners(bus);
    bus.addListener(this::onStatAward);
  }

  private void onStatAward(StatAwardEvent event) {
    if (this.isCompleted() || this.getParent() == null) return;
    if (
      event.getEntity().equals(this.getParent().manager.player) &&
      event.getEntity() instanceof ServerPlayer &&
      event.getStat().equals(this.getStat())
    ) {
      this.setUnits(this.getUnits() + event.getAmount());
    }
  }

  private Stat<ResourceLocation> getStat() {
    return Stats.CUSTOM.get(this.stat);
  }

  private int getStatValue() {
    return Objects.requireNonNull(Util.getStats(this.getParent().manager.player)).getValue(this.getStat());
  }

  @Override
  public void writeInitialData(CompoundTag data) {
    super.writeInitialData(data);
    if (this.trackSinceStart) {
      this.statAtStart = this.getStatValue();
      data.putInt("statAtStart", this.statAtStart);
    }
  }

  @Override
  public CompoundTag serialize() {
    CompoundTag data = super.serialize();
    if (this.trackSinceStart) {
      data.putInt("statAtStart", this.statAtStart);
    }
    return data;
  }

  @Override
  public void deserialize(CompoundTag data) {
    super.deserialize(data);
    if (this.trackSinceStart) {
      this.statAtStart = data.getInt("statAtStart");
    }
  }

  // This fixes a weird bug:
  //  Stat.CUSTOM.get(new ResourceLocation("stat")) // -> NullPointerException
  //  Stat.CUSTOM.get(Stat.STAT) // -> Stat<ResourceLocation>
  // even though both arguments are ResourceLocations and equivalent...
  //
  // I've tracked it down to StatType using an internal MappedRegistry, where .getKey fails for some reason.
  // My guess is that MappedRegistry internally uses some weird comparison to get keys, and only checks if the instances are the same, and not hashes or .equals()
  // This is a hacky workaround to fix the issue.

  private static final Map<ResourceLocation, ResourceLocation> STAT_MAP = new HashMap<>();
  static {
    STAT_MAP.put(new ResourceLocation("leave_game"), Stats.LEAVE_GAME);
    STAT_MAP.put(new ResourceLocation("play_time"), Stats.PLAY_TIME);
    STAT_MAP.put(new ResourceLocation("total_world_time"), Stats.TOTAL_WORLD_TIME);
    STAT_MAP.put(new ResourceLocation("time_since_death"), Stats.TIME_SINCE_DEATH);
    STAT_MAP.put(new ResourceLocation("time_since_rest"), Stats.TIME_SINCE_REST);
    STAT_MAP.put(new ResourceLocation("sneak_time"), Stats.CROUCH_TIME);
    STAT_MAP.put(new ResourceLocation("walk_one_cm"), Stats.WALK_ONE_CM);
    STAT_MAP.put(new ResourceLocation("crouch_one_cm"), Stats.CROUCH_ONE_CM);
    STAT_MAP.put(new ResourceLocation("sprint_one_cm"), Stats.SPRINT_ONE_CM);
    STAT_MAP.put(new ResourceLocation("walk_on_water_one_cm"), Stats.WALK_ON_WATER_ONE_CM);
    STAT_MAP.put(new ResourceLocation("fall_one_cm"), Stats.FALL_ONE_CM);
    STAT_MAP.put(new ResourceLocation("climb_one_cm"), Stats.CLIMB_ONE_CM);
    STAT_MAP.put(new ResourceLocation("fly_one_cm"), Stats.FLY_ONE_CM);
    STAT_MAP.put(new ResourceLocation("walk_under_water_one_cm"), Stats.WALK_UNDER_WATER_ONE_CM);
    STAT_MAP.put(new ResourceLocation("minecart_one_cm"), Stats.MINECART_ONE_CM);
    STAT_MAP.put(new ResourceLocation("boat_one_cm"), Stats.BOAT_ONE_CM);
    STAT_MAP.put(new ResourceLocation("pig_one_cm"), Stats.PIG_ONE_CM);
    STAT_MAP.put(new ResourceLocation("horse_one_cm"), Stats.HORSE_ONE_CM);
    STAT_MAP.put(new ResourceLocation("aviate_one_cm"), Stats.AVIATE_ONE_CM);
    STAT_MAP.put(new ResourceLocation("swim_one_cm"), Stats.SWIM_ONE_CM);
    STAT_MAP.put(new ResourceLocation("strider_one_cm"), Stats.STRIDER_ONE_CM);
    STAT_MAP.put(new ResourceLocation("jump"), Stats.JUMP);
    STAT_MAP.put(new ResourceLocation("drop"), Stats.DROP);
    STAT_MAP.put(new ResourceLocation("damage_dealt"), Stats.DAMAGE_DEALT);
    STAT_MAP.put(new ResourceLocation("damage_dealt_absorbed"), Stats.DAMAGE_DEALT_ABSORBED);
    STAT_MAP.put(new ResourceLocation("damage_dealt_resisted"), Stats.DAMAGE_DEALT_RESISTED);
    STAT_MAP.put(new ResourceLocation("damage_taken"), Stats.DAMAGE_TAKEN);
    STAT_MAP.put(new ResourceLocation("damage_blocked_by_shield"), Stats.DAMAGE_BLOCKED_BY_SHIELD);
    STAT_MAP.put(new ResourceLocation("damage_absorbed"), Stats.DAMAGE_ABSORBED);
    STAT_MAP.put(new ResourceLocation("damage_resisted"), Stats.DAMAGE_RESISTED);
    STAT_MAP.put(new ResourceLocation("deaths"), Stats.DEATHS);
    STAT_MAP.put(new ResourceLocation("mob_kills"), Stats.MOB_KILLS);
    STAT_MAP.put(new ResourceLocation("animals_bred"), Stats.ANIMALS_BRED);
    STAT_MAP.put(new ResourceLocation("player_kills"), Stats.PLAYER_KILLS);
    STAT_MAP.put(new ResourceLocation("fish_caught"), Stats.FISH_CAUGHT);
    STAT_MAP.put(new ResourceLocation("talked_to_villager"), Stats.TALKED_TO_VILLAGER);
    STAT_MAP.put(new ResourceLocation("traded_with_villager"), Stats.TRADED_WITH_VILLAGER);
    STAT_MAP.put(new ResourceLocation("eat_cake_slice"), Stats.EAT_CAKE_SLICE);
    STAT_MAP.put(new ResourceLocation("fill_cauldron"), Stats.FILL_CAULDRON);
    STAT_MAP.put(new ResourceLocation("use_cauldron"), Stats.USE_CAULDRON);
    STAT_MAP.put(new ResourceLocation("clean_armor"), Stats.CLEAN_ARMOR);
    STAT_MAP.put(new ResourceLocation("clean_banner"), Stats.CLEAN_BANNER);
    STAT_MAP.put(new ResourceLocation("clean_shulker_box"), Stats.CLEAN_SHULKER_BOX);
    STAT_MAP.put(new ResourceLocation("interact_with_brewingstand"), Stats.INTERACT_WITH_BREWINGSTAND);
    STAT_MAP.put(new ResourceLocation("interact_with_beacon"), Stats.INTERACT_WITH_BEACON);
    STAT_MAP.put(new ResourceLocation("inspect_dropper"), Stats.INSPECT_DROPPER);
    STAT_MAP.put(new ResourceLocation("inspect_hopper"), Stats.INSPECT_HOPPER);
    STAT_MAP.put(new ResourceLocation("inspect_dispenser"), Stats.INSPECT_DISPENSER);
    STAT_MAP.put(new ResourceLocation("play_noteblock"), Stats.PLAY_NOTEBLOCK);
    STAT_MAP.put(new ResourceLocation("tune_noteblock"), Stats.TUNE_NOTEBLOCK);
    STAT_MAP.put(new ResourceLocation("pot_flower"), Stats.POT_FLOWER);
    STAT_MAP.put(new ResourceLocation("trigger_trapped_chest"), Stats.TRIGGER_TRAPPED_CHEST);
    STAT_MAP.put(new ResourceLocation("open_enderchest"), Stats.OPEN_ENDERCHEST);
    STAT_MAP.put(new ResourceLocation("enchant_item"), Stats.ENCHANT_ITEM);
    STAT_MAP.put(new ResourceLocation("play_record"), Stats.PLAY_RECORD);
    STAT_MAP.put(new ResourceLocation("interact_with_furnace"), Stats.INTERACT_WITH_FURNACE);
    STAT_MAP.put(new ResourceLocation("interact_with_crafting_table"), Stats.INTERACT_WITH_CRAFTING_TABLE);
    STAT_MAP.put(new ResourceLocation("open_chest"), Stats.OPEN_CHEST);
    STAT_MAP.put(new ResourceLocation("sleep_in_bed"), Stats.SLEEP_IN_BED);
    STAT_MAP.put(new ResourceLocation("open_shulker_box"), Stats.OPEN_SHULKER_BOX);
    STAT_MAP.put(new ResourceLocation("open_barrel"), Stats.OPEN_BARREL);
    STAT_MAP.put(new ResourceLocation("interact_with_blast_furnace"), Stats.INTERACT_WITH_BLAST_FURNACE);
    STAT_MAP.put(new ResourceLocation("interact_with_smoker"), Stats.INTERACT_WITH_SMOKER);
    STAT_MAP.put(new ResourceLocation("interact_with_lectern"), Stats.INTERACT_WITH_LECTERN);
    STAT_MAP.put(new ResourceLocation("interact_with_campfire"), Stats.INTERACT_WITH_CAMPFIRE);
    STAT_MAP.put(new ResourceLocation("interact_with_cartography_table"), Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
    STAT_MAP.put(new ResourceLocation("interact_with_loom"), Stats.INTERACT_WITH_LOOM);
    STAT_MAP.put(new ResourceLocation("interact_with_stonecutter"), Stats.INTERACT_WITH_STONECUTTER);
    STAT_MAP.put(new ResourceLocation("bell_ring"), Stats.BELL_RING);
    STAT_MAP.put(new ResourceLocation("raid_trigger"), Stats.RAID_TRIGGER);
    STAT_MAP.put(new ResourceLocation("raid_win"), Stats.RAID_WIN);
    STAT_MAP.put(new ResourceLocation("interact_with_anvil"), Stats.INTERACT_WITH_ANVIL);
    STAT_MAP.put(new ResourceLocation("interact_with_grindstone"), Stats.INTERACT_WITH_GRINDSTONE);
    STAT_MAP.put(new ResourceLocation("target_hit"), Stats.TARGET_HIT);
    STAT_MAP.put(new ResourceLocation("interact_with_smithing_table"), Stats.INTERACT_WITH_SMITHING_TABLE);
  }
}

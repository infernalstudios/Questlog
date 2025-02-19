package org.infernalstudios.questlog.event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class QLBlockEvent extends QLEvent {
  public final BlockState state;
  public final BlockPos pos;
  @Nullable
  public final LivingEntity entity;

  public QLBlockEvent(BlockState state, BlockPos pos, @Nullable LivingEntity entity) {
    this.state = state;
    this.pos = pos;
    this.entity = entity;
  }

  public static class Place extends QLBlockEvent {
    public Place(BlockState state, BlockPos pos, @Nullable LivingEntity entity) {
      super(state, pos, entity);
    }
  }
  public static class Break extends QLBlockEvent {
    public Break(BlockState state, BlockPos pos, @Nullable LivingEntity entity) {
      super(state, pos, entity);
    }
  }
  public static class FarmlandTrample extends QLBlockEvent {
    public FarmlandTrample(BlockState state, BlockPos pos, @Nullable LivingEntity entity) {
      super(state, pos, entity);
    }
  }
}

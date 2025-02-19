package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.CachedRegistryPredicate;
import org.infernalstudios.questlog.util.JsonUtils;

public class AbstractBlockObjective extends Objective {

  private final CachedRegistryPredicate<Block> block;

  public AbstractBlockObjective(JsonObject definition) {
    super(definition);
    this.block = new CachedRegistryPredicate<>(
      JsonUtils.getString(definition, "block"),
      BuiltInRegistries.BLOCK,
      Object::equals,
      (tag, block) -> block.defaultBlockState().is(tag)
    );
  }

  protected boolean test(Block block) {
    return this.block.test(block);
  }

  protected boolean test(BlockState block) {
    return this.test(block.getBlock());
  }
}

package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.util.JsonUtils;

import javax.annotation.Nullable;

public class AbstractBlockObjective extends Objective {
  private final String block;
  @Nullable
  private Block cachedBlock = null;
  @Nullable
  private TagKey<Block> cachedTag = null;

  public AbstractBlockObjective(JsonObject definition) {
    super(definition);

    this.block = JsonUtils.getString(definition, "block");
  }

  private TagKey<Block> getTag() {
    if (this.cachedTag == null) {
      ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
      if (tags == null) throw new IllegalStateException("Block tags are not available yet");
      this.cachedTag = tags.createTagKey(new ResourceLocation(this.block.substring(1)));
    }

    return this.cachedTag;
  }

  private Block getBlock() {
    if (this.cachedBlock == null) {
      this.cachedBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.block));
    }

    return this.cachedBlock;
  }

  protected boolean test(Block block) {
    if (this.block.startsWith("#")) {
      return block.defaultBlockState().is(this.getTag());
    } else {
      return block.equals(this.getBlock());
    }
  }

  protected boolean test(BlockState state) {
    if (this.block.startsWith("#")) {
      return state.is(this.getTag());
    } else {
      return state.is(this.getBlock());
    }
  }
}

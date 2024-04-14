package org.infernalstudios.questlog.core.quests.objectives.block;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

import javax.annotation.Nullable;

public class AbstractBlockObjective extends Objective {
  private final ResourceLocation block;
  @Nullable
  private Block cachedBlock = null;

  public AbstractBlockObjective(JsonObject definition) {
    super(definition);

    this.block = new ResourceLocation(definition.get("block").getAsString());
  }

  protected Block getBlock() {
    if (this.cachedBlock == null) {
      this.cachedBlock = ForgeRegistries.BLOCKS.getValue(this.block);
    }

    return this.cachedBlock;
  }
}

package org.infernalstudios.questlog.util;

import net.minecraft.nbt.CompoundTag;

public interface NbtSaveable {
  /**
   * This method is used to write the initial data to a CompoundTag.<p>
   * This method must set defaults for a quest, which later may be changed.<p>
   * Do not write data that cannot be inferred from the definition (i.e. data written must be dependent on game factors)
   *
   * @param data The CompoundTag instance to which the initial data will be written.
   */
  void writeInitialData(CompoundTag data);

  /**
   * This method is used to read data.
   *
   * @param data The CompoundTag instance from which the data will be read.
   */
  void deserialize(CompoundTag data);

  /**
   * This method is used to write data to a new CompoundTag and return it.<p>
   * This must follow the same schema as {@linkplain NbtSaveable#writeInitialData(CompoundTag)}
   *
   * @return A CompoundTag instance containing the data from the Objective class.
   */
  CompoundTag serialize();
}

package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.util.JsonUtils;

public class CommandReward extends Reward {
  private final String command;
  private final int permissionLevel;

  public CommandReward(JsonObject definition) {
    super(definition);

    this.command = JsonUtils.getString(definition, "command");
    this.permissionLevel = JsonUtils.getOrDefault(definition, "permission_level", 2);
  }

  @Override
  public void applyReward(ServerPlayer player) {
    CommandSourceStack source = player.createCommandSourceStack()
      .withEntity(player)
      .withPosition(player.position())
      .withPermission(this.permissionLevel)
      .withSuppressedOutput();

    player.getServer().getCommands().performPrefixedCommand(source, this.command);

    super.applyReward(player);
  }
}

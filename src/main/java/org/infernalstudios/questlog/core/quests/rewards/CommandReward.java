package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class CommandReward extends Reward {
  private final String command;
  private final int permissionLevel;

  public CommandReward(JsonObject definition) {
    super(definition);

    this.command = definition.get("command").getAsString();
    if (definition.has("permission_level")) {
      this.permissionLevel = definition.get("permission_level").getAsInt();
    } else {
      this.permissionLevel = 2;
    }
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

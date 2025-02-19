package org.infernalstudios.questlog.network;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IPacketContext {
  // Null if the packet is sent from the server
  // If it's null, but direction is CLIENT_TO_SERVER, something is wrong
  @Nullable
  Player getSender();

  Direction getDirection();

  enum Direction { CLIENT_TO_SERVER, SERVER_TO_CLIENT }
}

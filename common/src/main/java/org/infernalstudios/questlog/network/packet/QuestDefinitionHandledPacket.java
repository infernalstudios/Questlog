package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.network.IPacketContext;

public class QuestDefinitionHandledPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;
  private final ResourceLocation id;

  public QuestDefinitionHandledPacket(ResourceLocation id) {
    this.id = id;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
  }

  public static QuestDefinitionHandledPacket decode(FriendlyByteBuf buf) {
    ResourceLocation id = buf.readResourceLocation();
    return new QuestDefinitionHandledPacket(id);
  }

  public static void handle(QuestDefinitionHandledPacket packet, IPacketContext ctx) {
    Questlog.LOGGER.trace("Client handled definition for quest {}", packet.id.toString());

    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(ctx.getSender());
    manager.sync(packet.id);
  }
}

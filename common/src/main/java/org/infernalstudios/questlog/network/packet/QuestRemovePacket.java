package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.network.IPacketContext;

public class QuestRemovePacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

  private final ResourceLocation id;

  public QuestRemovePacket(ResourceLocation id) {
    this.id = id;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
  }

  public static QuestRemovePacket decode(FriendlyByteBuf buf) {
    ResourceLocation id = buf.readResourceLocation();
    return new QuestRemovePacket(id);
  }

  public static void handle(QuestRemovePacket packet, IPacketContext ctx) {
    Questlog.LOGGER.trace("Received remove packet for quest {}", packet.id.toString());
    QuestManager manager = QuestlogClient.getLocal();
    manager.removeQuest(packet.id);
  }
}

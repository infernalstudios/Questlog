package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogEvents;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.IPacketContext;

// Indicates to the client that a quest has been freshly completed
// and a notification may be sent.
// Sent only to notify the client to post a QuestCompletedEvent.
public class QuestCompletedPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

  private final ResourceLocation id;

  public QuestCompletedPacket(ResourceLocation id) {
    this.id = id;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
  }

  public static QuestCompletedPacket decode(FriendlyByteBuf buf) {
    return new QuestCompletedPacket(buf.readResourceLocation());
  }

  public static void handle(QuestCompletedPacket packet, IPacketContext ctx) {
    QuestManager manager = QuestlogClient.getLocal();
    QuestlogEvents.onQuestCompleted(new QuestEvent.Completed(manager.player, manager.getQuest(packet.id), false));
  }
}

package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.QuestlogEvents;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.events.QuestEvent;
import org.infernalstudios.questlog.network.IPacketContext;

// Indicates to the client that a quest has been freshly triggered
// and should be displayed in the quest log.
// Sent only to notify the client to post a QuestTriggeredEvent.
public class QuestTriggeredPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

  private final ResourceLocation id;

  public QuestTriggeredPacket(ResourceLocation id) {
    this.id = id;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
  }

  public static QuestTriggeredPacket decode(FriendlyByteBuf buf) {
    return new QuestTriggeredPacket(buf.readResourceLocation());
  }

  public static void handle(QuestTriggeredPacket packet, IPacketContext ctx) {
    QuestManager manager = QuestlogClient.getLocal();
    QuestlogEvents.onQuestTriggered(new QuestEvent.Triggered(manager.player, manager.getQuest(packet.id), false));
  }
}

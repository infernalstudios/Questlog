package org.infernalstudios.questlog.network.packet;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.QuestTriggeredEvent;

// Indicates to the client that a quest has been freshly triggered
// and should be displayed in the quest log.
// Sent only to notify the client to post a QuestTriggeredEvent.
public class QuestTriggeredPacket {

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

  public static void handle(QuestTriggeredPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isServer()) {
      throw new IllegalStateException("Client should not send QuestTriggeredPacket");
    }

    QuestManager manager = QuestManager.getLocal();
    MinecraftForge.EVENT_BUS.post(new QuestTriggeredEvent(manager.player, manager.getQuest(packet.id), true));
  }
}

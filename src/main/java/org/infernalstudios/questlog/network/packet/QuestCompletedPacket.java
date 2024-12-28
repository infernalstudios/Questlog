package org.infernalstudios.questlog.network.packet;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.event.QuestCompletedEvent;

// Indicates to the client that a quest has been freshly completed
// and a notification may be sent.
// Sent only to notify the client to post a QuestCompletedEvent.
public class QuestCompletedPacket {

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

  public static void handle(QuestCompletedPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isServer()) {
      throw new IllegalStateException("Client should not send QuestCompletedPacket");
    }

    QuestManager manager = QuestManager.getLocal();
    MinecraftForge.EVENT_BUS.post(new QuestCompletedEvent(manager.player, manager.getQuest(packet.id), true));
  }
}

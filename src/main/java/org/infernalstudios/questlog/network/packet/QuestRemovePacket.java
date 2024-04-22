package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;

import java.util.function.Supplier;

public class QuestRemovePacket {
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

  public static void handle(QuestRemovePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isServer()) {
      throw new IllegalStateException("Client should not send QuestRemovePacket");
    }

    Questlog.LOGGER.trace("Received remove packet for quest {}", packet.id.toString());

    QuestManager manager = QuestManager.getLocal();

    manager.removeQuest(packet.id);
  }
}

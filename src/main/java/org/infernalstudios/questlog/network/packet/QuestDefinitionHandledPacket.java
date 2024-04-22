package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;

import java.util.function.Supplier;

public class QuestDefinitionHandledPacket {
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

  public static void handle(QuestDefinitionHandledPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isClient()) {
      throw new IllegalStateException("Server should not send QuestDefinitionHandledPacket");
    }

    Questlog.LOGGER.trace("Client handled definition for quest {}", packet.id.toString());

    QuestManager manager = QuestManager.getLocal();
    manager.sync(packet.id);
  }
}

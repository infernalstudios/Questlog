package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;

import java.util.function.Supplier;

public class QuestDataPacket {
  private final ResourceLocation id;
  private final CompoundTag data;

  public QuestDataPacket(ResourceLocation id, CompoundTag data) {
    this.id = id;
    this.data = data;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
    buf.writeNbt(this.data);
  }

  public static QuestDataPacket decode(FriendlyByteBuf buf) {
    ResourceLocation id = buf.readResourceLocation();
    CompoundTag data = buf.readNbt();

    return new QuestDataPacket(id, data);
  }

  public static void handle(QuestDataPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isServer()) {
      throw new IllegalStateException("Client should not send QuestDataPacket");
    }

    try {
      QuestManager manager = QuestManager.getLocal();

      Quest quest = manager.getQuest(packet.id);
      if (quest == null) {
        throw new IllegalStateException("Quest is null, likely definition not loaded yet");
      }

      quest.deserialize(packet.data);
    } catch (Throwable e) {
      Questlog.LOGGER.error("Failed to handle QuestDataPacket", e);
    }
  }
}

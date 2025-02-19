package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;

public class QuestDataPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;
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

  public static void handle(QuestDataPacket packet, IPacketContext ctx) {
    try {
      QuestManager manager = QuestlogClient.getLocal();

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

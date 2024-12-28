package org.infernalstudios.questlog.network.packet;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;

// Indicates to the server that the client has collected a reward in the GUI.
public class QuestRewardCollectPacket {

  private final ResourceLocation id;
  private final int rewardIndex;

  public QuestRewardCollectPacket(ResourceLocation id, int rewardIndex) {
    this.id = id;
    this.rewardIndex = rewardIndex;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);
    buf.writeInt(this.rewardIndex);
  }

  public static QuestRewardCollectPacket decode(FriendlyByteBuf buf) {
    return new QuestRewardCollectPacket(buf.readResourceLocation(), buf.readInt());
  }

  public static void handle(QuestRewardCollectPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isClient()) {
      throw new IllegalStateException("Server should not send QuestRewardCollectPacket");
    }

    QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(ctx.getSender());
    Quest quest = manager.getQuest(packet.id);
    if (quest == null) {
      Questlog.LOGGER.warn("Quest {} not found", packet.id);
      return;
    }
    Reward reward = quest.rewards.get(packet.rewardIndex);
    if (reward == null) {
      Questlog.LOGGER.warn("Reward {} not found in quest {}", packet.rewardIndex, packet.id);
      return;
    }
    if (!reward.hasRewarded()) {
      reward.applyReward((ServerPlayer) manager.player);
    }
  }
}

package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.IPacketContext;

// Indicates to the server that the client has collected a reward in the GUI.
public class QuestRewardCollectPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

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

  public static void handle(QuestRewardCollectPacket packet, IPacketContext ctx) {
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

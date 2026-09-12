package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.ChoiceReward;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.IPacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record QuestRewardCollectPacket(ResourceLocation id, int rewardIndex, List<Integer> selections) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestRewardCollectPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        int rewardIndex = buf.readInt();
        int selectionCount = buf.readVarInt();
        List<Integer> selections = new ArrayList<>(selectionCount);
        for (int i = 0; i < selectionCount; i++) {
            selections.add(buf.readInt());
        }
        return new QuestRewardCollectPacket(id, rewardIndex, selections);
    }

    public static void handle(QuestRewardCollectPacket packet, IPacketContext ctx) {
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(Objects.requireNonNull(ctx.getSender()));
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
            if (reward instanceof ChoiceReward choiceReward) {
                choiceReward.setSelectedIndices(packet.selections());
            }
            reward.applyReward((ServerPlayer) manager.player);
            ServerPlayerManager.INSTANCE.save(manager);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeInt(this.rewardIndex);
        buf.writeVarInt(this.selections.size());
        for (int sel : this.selections) {
            buf.writeInt(sel);
        }
    }
}

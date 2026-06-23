package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record QuestRewardCollectPacket(ResourceLocation id, int rewardIndex, java.util.List<Integer> selections) implements CustomPacketPayload {
    public static final Type<QuestRewardCollectPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "reward_collect"));

    public QuestRewardCollectPacket(ResourceLocation id, int rewardIndex) {
        this(id, rewardIndex, java.util.Collections.emptyList());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestRewardCollectPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestRewardCollectPacket::id,
            ByteBufCodecs.INT, QuestRewardCollectPacket::rewardIndex,
            ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.INT), QuestRewardCollectPacket::selections,
            QuestRewardCollectPacket::new
    );

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
            if (reward instanceof org.infernalstudios.questlog.core.quests.rewards.ChoiceReward choiceReward) {
                choiceReward.setSelectedIndices(packet.selections());
            }
            reward.applyReward((ServerPlayer) manager.player);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
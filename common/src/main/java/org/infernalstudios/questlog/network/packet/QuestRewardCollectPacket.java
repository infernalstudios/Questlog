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

public class QuestRewardCollectPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuestRewardCollectPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "reward_collect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestRewardCollectPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestRewardCollectPacket::id,
            ByteBufCodecs.INT, QuestRewardCollectPacket::rewardIndex,
            QuestRewardCollectPacket::new
    );

    private final ResourceLocation id;
    private final int rewardIndex;

    public QuestRewardCollectPacket(ResourceLocation id, int rewardIndex) {
        this.id = id;
        this.rewardIndex = rewardIndex;
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

    public ResourceLocation id() {
        return id;
    }

    public int rewardIndex() {
        return rewardIndex;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
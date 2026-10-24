package org.infernalstudios.questlog.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.network.IPacketContext;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
//?} else {
//import net.minecraft.network.FriendlyByteBuf;
//?}

import java.util.Objects;
//? if <1.21 {
/*import java.util.ArrayList;
import java.util.List;
*///?}

//? if >=1.21 {
public record QuestRewardCollectPacket(ResourceLocation id, int rewardIndex,
                                       java.util.List<Integer> selections) implements CustomPacketPayload {
//?} else {
//public record QuestRewardCollectPacket(ResourceLocation id, int rewardIndex, List<Integer> selections) {
//?}
    //? if >=1.21 {
    public static final Type<QuestRewardCollectPacket> TYPE = new Type<>(Questlog.id("reward_collect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestRewardCollectPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestRewardCollectPacket::id,
            ByteBufCodecs.INT, QuestRewardCollectPacket::rewardIndex,
            ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.INT), QuestRewardCollectPacket::selections,
            QuestRewardCollectPacket::new
    );
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

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

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
        buf.writeInt(this.rewardIndex);
        buf.writeVarInt(this.selections.size());
        for (int sel : this.selections) {
            buf.writeInt(sel);
        }
    }
    *///?}

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
            ServerPlayerManager.INSTANCE.save(manager);
        }
    }

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

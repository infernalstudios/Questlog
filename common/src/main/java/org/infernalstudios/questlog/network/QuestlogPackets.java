package org.infernalstudios.questlog.network;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.packet.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuestlogPackets {
  public static final List<RegisteredPacket<?>> PACKETS = new ImmutableList.Builder<RegisteredPacket<?>>()
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "data"), QuestDataPacket.class, QuestDataPacket.DIRECTION, QuestDataPacket::encode, QuestDataPacket::decode, QuestDataPacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "definition"), QuestDefinitionPacket.class, QuestDefinitionPacket.DIRECTION, QuestDefinitionPacket::encode, QuestDefinitionPacket::decode, QuestDefinitionPacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "definition_handled"), QuestDefinitionHandledPacket.class, QuestDefinitionHandledPacket.DIRECTION, QuestDefinitionHandledPacket::encode, QuestDefinitionHandledPacket::decode, QuestDefinitionHandledPacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "remove"), QuestRemovePacket.class, QuestRemovePacket.DIRECTION, QuestRemovePacket::encode, QuestRemovePacket::decode, QuestRemovePacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "triggered"), QuestTriggeredPacket.class, QuestTriggeredPacket.DIRECTION, QuestTriggeredPacket::encode, QuestTriggeredPacket::decode, QuestTriggeredPacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "completed"), QuestCompletedPacket.class, QuestCompletedPacket.DIRECTION, QuestCompletedPacket::encode, QuestCompletedPacket::decode, QuestCompletedPacket::handle))
    .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "reward_collect"), QuestRewardCollectPacket.class, QuestRewardCollectPacket.DIRECTION, QuestRewardCollectPacket::encode, QuestRewardCollectPacket::decode, QuestRewardCollectPacket::handle))
    .build();

  public record RegisteredPacket<T>(ResourceLocation id, Class<T> clazz, IPacketContext.Direction direction, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, IPacketContext> handler) {}
}

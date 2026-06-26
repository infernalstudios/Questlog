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
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "remove"), QuestRemovePacket.class, QuestRemovePacket.DIRECTION, QuestRemovePacket::encode, QuestRemovePacket::decode, QuestRemovePacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "triggered"), QuestTriggeredPacket.class, QuestTriggeredPacket.DIRECTION, QuestTriggeredPacket::encode, QuestTriggeredPacket::decode, QuestTriggeredPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "completed"), QuestCompletedPacket.class, QuestCompletedPacket.DIRECTION, QuestCompletedPacket::encode, QuestCompletedPacket::decode, QuestCompletedPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "reward_collect"), QuestRewardCollectPacket.class, QuestRewardCollectPacket.DIRECTION, QuestRewardCollectPacket::encode, QuestRewardCollectPacket::decode, QuestRewardCollectPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "sync"), QuestSyncPacket.class, QuestSyncPacket.DIRECTION, QuestSyncPacket::encode, QuestSyncPacket::decode, QuestSyncPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "open"), QuestOpenPacket.class, QuestOpenPacket.DIRECTION, QuestOpenPacket::encode, QuestOpenPacket::decode, QuestOpenPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "edit_mode"), QuestEditModePacket.class, QuestEditModePacket.DIRECTION, QuestEditModePacket::encode, QuestEditModePacket::decode, QuestEditModePacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "read"), QuestReadPacket.class, QuestReadPacket.DIRECTION, QuestReadPacket::encode, QuestReadPacket::decode, QuestReadPacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "edit_save"), QuestEditSavePacket.class, QuestEditSavePacket.DIRECTION, QuestEditSavePacket::encode, QuestEditSavePacket::decode, QuestEditSavePacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "edit_remove"), QuestEditRemovePacket.class, QuestEditRemovePacket.DIRECTION, QuestEditRemovePacket::encode, QuestEditRemovePacket::decode, QuestEditRemovePacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "chapter_edit_save"), ChapterEditSavePacket.class, ChapterEditSavePacket.DIRECTION, ChapterEditSavePacket::encode, ChapterEditSavePacket::decode, ChapterEditSavePacket::handle))
            .add(new RegisteredPacket<>(new ResourceLocation(Questlog.MODID, "chapter_edit_remove"), ChapterEditRemovePacket.class, ChapterEditRemovePacket.DIRECTION, ChapterEditRemovePacket::encode, ChapterEditRemovePacket::decode, ChapterEditRemovePacket::handle))
            .build();

    public record RegisteredPacket<T>(ResourceLocation id, Class<T> clazz, IPacketContext.Direction direction,
                                      BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                      BiConsumer<T, IPacketContext> handler) {
    }
}
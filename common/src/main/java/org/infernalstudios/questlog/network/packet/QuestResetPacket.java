package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record QuestResetPacket(ResourceLocation id) implements CustomPacketPayload {
    public static final Type<QuestResetPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Questlog.MODID, "reset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestResetPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, QuestResetPacket::id,
            QuestResetPacket::new
    );

    public static void handle(QuestResetPacket packet, IPacketContext ctx) {
        QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(Objects.requireNonNull(ctx.getSender()));
        Quest quest = manager.getQuest(packet.id);
        if (quest != null && quest.isRepeatable()) {
            if (quest.isGlobal()) {
                ServerPlayerManager.INSTANCE.resetGlobalQuest(quest.getId());
            } else {
                quest.resetProgress();
                ServerPlayerManager.INSTANCE.save(manager);
            }
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

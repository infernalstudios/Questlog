package org.infernalstudios.questlog.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;

import java.util.Objects;

public record QuestResetPacket(ResourceLocation id) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.CLIENT_TO_SERVER;

    public static QuestResetPacket decode(FriendlyByteBuf buf) {
        return new QuestResetPacket(buf.readResourceLocation());
    }

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

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.id);
    }
}

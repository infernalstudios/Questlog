package org.infernalstudios.questlog.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.client.gui.screen.QuestDetails;
import org.infernalstudios.questlog.client.gui.screen.QuestlogScreen;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;

public record QuestOpenPacket(String target) {
    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestOpenPacket decode(FriendlyByteBuf buf) {
        return new QuestOpenPacket(buf.readUtf());
    }

    public static void handle(QuestOpenPacket packet, IPacketContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String target = packet.target();
        if (target != null && !target.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(target);
            if (id != null) {
                Quest quest = QuestlogClient.getLocal().getQuest(id);
                if (quest != null) {
                    mc.setScreen(new QuestDetails(mc.screen, quest));
                    return;
                }
            }
        }

        mc.setScreen(new QuestlogScreen(mc.screen));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.target);
    }
}
package org.infernalstudios.questlog.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.packet.QuestCompletedPacket;
import org.infernalstudios.questlog.network.packet.QuestDataPacket;
import org.infernalstudios.questlog.network.packet.QuestDefinitionHandledPacket;
import org.infernalstudios.questlog.network.packet.QuestDefinitionPacket;
import org.infernalstudios.questlog.network.packet.QuestRemovePacket;
import org.infernalstudios.questlog.network.packet.QuestRewardCollectPacket;
import org.infernalstudios.questlog.network.packet.QuestTriggeredPacket;

public class NetworkHandler {
  private static final String PROTOCOL_VERSION = "1";
  
  public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
    new ResourceLocation(Questlog.MODID, "main"),
    () -> PROTOCOL_VERSION,
    PROTOCOL_VERSION::equals,
    PROTOCOL_VERSION::equals
  );

  private static int PACKET_ID_COUNTER = 0;

  public static synchronized void register() {
    Questlog.LOGGER.debug("Registering network packets");

    INSTANCE.messageBuilder(QuestDataPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestDataPacket::encode)
      .decoder(QuestDataPacket::decode)
      .consumerMainThread(QuestDataPacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestDefinitionPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestDefinitionPacket::encode)
      .decoder(QuestDefinitionPacket::decode)
      .consumerMainThread(QuestDefinitionPacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestDefinitionHandledPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestDefinitionHandledPacket::encode)
      .decoder(QuestDefinitionHandledPacket::decode)
      .consumerMainThread(QuestDefinitionHandledPacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestRemovePacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestRemovePacket::encode)
      .decoder(QuestRemovePacket::decode)
      .consumerMainThread(QuestRemovePacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestTriggeredPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestTriggeredPacket::encode)
      .decoder(QuestTriggeredPacket::decode)
      .consumerMainThread(QuestTriggeredPacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestCompletedPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestCompletedPacket::encode)
      .decoder(QuestCompletedPacket::decode)
      .consumerMainThread(QuestCompletedPacket::handle)
      .add();

    INSTANCE.messageBuilder(QuestRewardCollectPacket.class, PACKET_ID_COUNTER++)
      .encoder(QuestRewardCollectPacket::encode)
      .decoder(QuestRewardCollectPacket::decode)
      .consumerMainThread(QuestRewardCollectPacket::handle)
      .add();
  }

  public static <M> void sendToPlayer(M message, ServerPlayer player) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  public static <M> void sendToServer(M message) {
    INSTANCE.sendToServer(message);
  }
}

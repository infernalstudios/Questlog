package org.infernalstudios.questlog.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.packet.QuestCompletedPacket;
import org.infernalstudios.questlog.network.packet.QuestDataPacket;
import org.infernalstudios.questlog.network.packet.QuestDefinitionHandledPacket;
import org.infernalstudios.questlog.network.packet.QuestDefinitionPacket;
import org.infernalstudios.questlog.network.packet.QuestRemovePacket;
import org.infernalstudios.questlog.network.packet.QuestTriggeredPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    registerPacket(QuestDataPacket.class);
    registerPacket(QuestDefinitionPacket.class);
    registerPacket(QuestDefinitionHandledPacket.class);
    registerPacket(QuestRemovePacket.class);
    registerPacket(QuestTriggeredPacket.class);
    registerPacket(QuestCompletedPacket.class);
  }

  @SuppressWarnings("unchecked")
  private static synchronized <M> void registerPacket(Class<M> packetClass) {
    BiConsumer<M, FriendlyByteBuf> encode;
    Function<FriendlyByteBuf, M> decode;
    BiConsumer<M, Supplier<NetworkEvent.Context>> handle;
    try {
      Method encodeMethod = packetClass.getMethod("encode", FriendlyByteBuf.class);
      if (encodeMethod.getReturnType() != void.class) {
        throw new IllegalArgumentException("Encode method must return void");
      }
      encode = (packet, friendlyByteBuf) -> {
        try {
          encodeMethod.invoke(packet, friendlyByteBuf);
        } catch (IllegalAccessException | InvocationTargetException e) {
          Questlog.LOGGER.error("Failed to encode " + packetClass.getName(), e);
          throw new RuntimeException(e);
        }
      };
      Method decodeMethod = packetClass.getMethod("decode", FriendlyByteBuf.class);
      if (decodeMethod.getReturnType() != packetClass) {
        throw new IllegalArgumentException("Decode method must return the packet class");
      }
      decode = (friendlyByteBuf) -> {
        try {
          return (M) decodeMethod.invoke(null, friendlyByteBuf);
        } catch (IllegalAccessException | InvocationTargetException e) {
          Questlog.LOGGER.error("Failed to decode " + packetClass.getName(), e);
          throw new RuntimeException(e);
        }
      };
      Method handleMethod = packetClass.getMethod("handle", packetClass, Supplier.class);
      if (handleMethod.getReturnType() != void.class) {
        throw new IllegalArgumentException("Handle method must return void");
      }
      handle = (packet, supplier) -> {
        try {
          handleMethod.invoke(null, packet, supplier);
        } catch (IllegalAccessException | InvocationTargetException e) {
          Questlog.LOGGER.error("Failed to handle " + packetClass.getName(), e);
          throw new RuntimeException(e);
        }
      };
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    INSTANCE.messageBuilder(packetClass, PACKET_ID_COUNTER++).encoder(encode).decoder(decode).consumerMainThread(handle).add();
  }

  public static <M> void sendToPlayer(M message, ServerPlayer player) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  public static <M> void sendToServer(M message) {
    INSTANCE.sendToServer(message);
  }
}

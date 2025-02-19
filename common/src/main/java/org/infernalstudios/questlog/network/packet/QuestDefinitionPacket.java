package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.QuestlogClient;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.IPacketContext;
import org.infernalstudios.questlog.platform.Services;

public class QuestDefinitionPacket {
  public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

  private static final Gson GSON = new GsonBuilder().create();

  private final ResourceLocation id;
  private final JsonObject definition;

  public QuestDefinitionPacket(ResourceLocation id, JsonObject definition) {
    this.id = id;
    this.definition = definition;
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(this.id);

    String json = GSON.toJson(this.definition);
    buf.writeInt(json.getBytes(StandardCharsets.UTF_8).length);
    buf.writeCharSequence(json, StandardCharsets.UTF_8);
  }

  public static QuestDefinitionPacket decode(FriendlyByteBuf buf) {
    ResourceLocation id = buf.readResourceLocation();

    int length = buf.readInt();
    String json = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
    JsonObject definition = GSON.fromJson(json, JsonObject.class);

    return new QuestDefinitionPacket(id, definition);
  }

  public static void handle(QuestDefinitionPacket packet, IPacketContext ctx) {
    if (Minecraft.getInstance().player == null) {
      defer(packet);
      return;
    }
    try {
      QuestManager manager = QuestlogClient.getLocal();

      Quest quest = Quest.create(Objects.requireNonNull(packet.definition), packet.id, manager);
      manager.addQuest(quest);

      Services.PLATFORM.sendPacketToServer(new QuestDefinitionHandledPacket(packet.id));
    } catch (Throwable e) {
      Questlog.LOGGER.error("Failed to handle QuestDefinitionPacket", e);
    }
  }

  // Theoretically, deferred packets will not be added when we call handleDeferred,
  // But just in case... we use a concurrent list.
  private static final List<QuestDefinitionPacket> DEFERRED = new CopyOnWriteArrayList<>();

  private static void defer(QuestDefinitionPacket packet) {
    DEFERRED.add(packet);
  }

  public static void handleDeferred() {
    Questlog.LOGGER.debug("Handling deferred QuestDefinitionPackets");
    for (QuestDefinitionPacket packet : DEFERRED) {
      handle(packet, null);
    }
    DEFERRED.clear();
  }
}

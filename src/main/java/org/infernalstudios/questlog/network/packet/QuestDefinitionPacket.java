package org.infernalstudios.questlog.network.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.network.NetworkHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public class QuestDefinitionPacket {
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

  public static void handle(QuestDefinitionPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
    NetworkEvent.Context ctx = ctxSupplier.get();

    if (ctx.getDirection().getReceptionSide().isServer()) {
      throw new IllegalStateException("Client should not send QuestDefinitionPacket");
    }

    try {
      QuestManager manager = QuestManager.getLocal();
  
      Quest quest = Quest.create(Objects.requireNonNull(packet.definition), packet.id, manager);
      manager.addQuest(quest);

      NetworkHandler.sendToServer(new QuestDefinitionHandledPacket(packet.id));
    } catch (Throwable e) {
      Questlog.LOGGER.error("Failed to handle QuestDefinitionPacket", e);
    }
  }
}

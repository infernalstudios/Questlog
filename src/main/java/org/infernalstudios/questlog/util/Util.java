package org.infernalstudios.questlog.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Util {
  private Util() {}  // Uninstantiable
  
  @CheckForNull // No stats counter ONLY for other players, when calling from the client
  public static StatsCounter getStats(Player player) {
    if (player instanceof LocalPlayer localPlayer) {
      return localPlayer.getStats();
    } else if (player instanceof ServerPlayer serverPlayer) {
      return serverPlayer.getStats();
    }
    
    return null;
  }

  public static <T> List<T> invertList(List<T> list) {
    List<T> inverted = new ArrayList<>(list.size());
    for (int i = list.size() - 1; i >= 0; i--) {
      inverted.add(list.get(i));
    }
    return inverted;
  }

  public static <T> Tag toNbtList(List<T> list, Function<T, Tag> serializer) {
    ListTag tag = new ListTag();
    tag.addAll(list.stream().map(serializer).toList());
    return tag;
  }

  private static final Gson GSON = new GsonBuilder().create();

  public static JsonObject getJsonResource(Resource resource) throws IOException {
    try (InputStream stream = resource.open()) {
      return GSON.fromJson(new String(stream.readAllBytes()), JsonObject.class);
    }
  }

  public static JsonObject getJsonResource(ResourceManager manager, ResourceLocation id) throws IOException {
    ResourceLocation path = new ResourceLocation(id.getNamespace(), id.getPath() + ".json");
    List<Resource> resources = manager.getResourceStack(path);

    if (resources.isEmpty()) {
      throw new IOException("Resource not found: " + path);
    }

    if (resources.size() > 1) {
      throw new IOException("Multiple resources found: " + path);
    }

    return Util.getJsonResource(resources.get(0));
  }

  public static BoundingBox bbFromJson(JsonElement json) {
    if (json instanceof JsonObject jsonObject) {
      int x1 = jsonObject.has("x1") ? jsonObject.get("x1").getAsInt() : Integer.MIN_VALUE;
      int y1 = jsonObject.has("y1") ? jsonObject.get("y1").getAsInt() : Integer.MIN_VALUE;
      int z1 = jsonObject.has("z1") ? jsonObject.get("z1").getAsInt() : Integer.MIN_VALUE;
      int x2 = jsonObject.has("x2") ? jsonObject.get("x2").getAsInt() : Integer.MAX_VALUE;
      int y2 = jsonObject.has("y2") ? jsonObject.get("y2").getAsInt() : Integer.MAX_VALUE;
      int z2 = jsonObject.has("z2") ? jsonObject.get("z2").getAsInt() : Integer.MAX_VALUE;

      if (x1 > x2) {
        int temp = x1;
        x1 = x2;
        x2 = temp;
      }

      if (y1 > y2) {
        int temp = y1;
        y1 = y2;
        y2 = temp;
      }

      if (z1 > z2) {
        int temp = z1;
        z1 = z2;
        z2 = temp;
      }

      return new BoundingBox(x1, y1, z1, x2, y2, z2);
    } else {
      return new BoundingBox(0, 0, 0, 0, 0, 0);
    }
  }

  public static void giveToPlayer(ServerPlayer player, ItemStack item) {
    boolean added = player.getInventory().add(item);
    if (added && item.isEmpty()) {
      item.setCount(1);
      ItemEntity itemEntity = player.drop(item, false);
      if (itemEntity != null) {
        itemEntity.makeFakeItem();
      }

      player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
      player.containerMenu.broadcastChanges();
    } else {
      ItemEntity itemEntity = player.drop(item, false);
      if (itemEntity != null) {
        itemEntity.setNoPickUpDelay();
        itemEntity.setOwner(player.getUUID());
      }
    }
  }
}

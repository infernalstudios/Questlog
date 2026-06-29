package org.infernalstudios.questlog.util;

import com.google.gson.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Util {

    private static final Gson GSON = new GsonBuilder().create();

    private Util() {
    }

    public static StatsCounter getStats(Player player) {
        // Important: check for ServerPlayer first, otherwise servers will crash
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getStats();
        } else if (player instanceof LocalPlayer localPlayer) {
            return localPlayer.getStats();
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

    public static JsonObject getJsonResource(Resource resource) throws IOException {
        try (InputStream stream = resource.open()) {
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return GSON.fromJson(content, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("Malformed JSON in resource " + resource + ": " + e.getMessage(), e);
        }
    }

    public static BoundingBox bbFromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }
        if (json.isJsonPrimitive()) {
            String str = json.getAsString().trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                try {
                    return bbFromJson(com.google.gson.JsonParser.parseString(str));
                } catch (Exception ignored) {
                }
            }
            String cleanStr = str.replaceAll("[^0-9,\\-\\s]", "").trim();
            String[] parts = cleanStr.split("[,\\s]+");
            if (parts.length >= 6) {
                try {
                    int x1 = Integer.parseInt(parts[0]);
                    int y1 = Integer.parseInt(parts[1]);
                    int z1 = Integer.parseInt(parts[2]);
                    int x2 = Integer.parseInt(parts[3]);
                    int y2 = Integer.parseInt(parts[4]);
                    int z2 = Integer.parseInt(parts[5]);
                    return new BoundingBox(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
                } catch (NumberFormatException ignored) {
                }
            } else if (parts.length >= 3) {
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    return new BoundingBox(x, y, z, x, y, z);
                } catch (NumberFormatException ignored) {
                }
            }
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }
        if (json.isJsonArray()) {
            com.google.gson.JsonArray arr = json.getAsJsonArray();
            if (arr.size() >= 6) {
                int x1 = arr.get(0).getAsInt();
                int y1 = arr.get(1).getAsInt();
                int z1 = arr.get(2).getAsInt();
                int x2 = arr.get(3).getAsInt();
                int y2 = arr.get(4).getAsInt();
                int z2 = arr.get(5).getAsInt();
                return new BoundingBox(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
            } else if (arr.size() >= 3) {
                int x = arr.get(0).getAsInt();
                int y = arr.get(1).getAsInt();
                int z = arr.get(2).getAsInt();
                return new BoundingBox(x, y, z, x, y, z);
            }
            return new BoundingBox(0, 0, 0, 0, 0, 0);
        }
        if (json instanceof JsonObject jsonObject) {
            int x1 = getIntOrDefault(jsonObject, new String[]{"x1", "minX", "min_x", "x"}, Integer.MIN_VALUE);
            int y1 = getIntOrDefault(jsonObject, new String[]{"y1", "minY", "min_y", "y"}, Integer.MIN_VALUE);
            int z1 = getIntOrDefault(jsonObject, new String[]{"z1", "minZ", "min_z", "z"}, Integer.MIN_VALUE);
            int x2 = getIntOrDefault(jsonObject, new String[]{"x2", "maxX", "max_x"}, x1 != Integer.MIN_VALUE ? x1 : Integer.MAX_VALUE);
            int y2 = getIntOrDefault(jsonObject, new String[]{"y2", "maxY", "max_y"}, y1 != Integer.MIN_VALUE ? y1 : Integer.MAX_VALUE);
            int z2 = getIntOrDefault(jsonObject, new String[]{"z2", "maxZ", "max_z"}, z1 != Integer.MIN_VALUE ? z1 : Integer.MAX_VALUE);

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

    private static int getIntOrDefault(JsonObject obj, String[] keys, int defaultValue) {
        for (String key : keys) {
            if (obj.has(key)) {
                return obj.get(key).getAsInt();
            }
        }
        return defaultValue;
    }

    public static void giveToPlayer(ServerPlayer player, ItemStack item) {
        boolean added = player.getInventory().add(item);
        if (added && item.isEmpty()) {
            item.setCount(1);
            ItemEntity itemEntity = player.drop(item, false);
            if (itemEntity != null) {
                itemEntity.makeFakeItem();
            }

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.2F,
                    ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
            );
            player.containerMenu.broadcastChanges();
        } else {
            ItemEntity itemEntity = player.drop(item, false);
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(player.getUUID());
            }
        }
    }
}

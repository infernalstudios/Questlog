package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, String> chapterDefinitions,
                              Map<ResourceLocation, CompoundTag> data,
                              List<ResourceLocation> advancements) {

    public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

    public static QuestSyncPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, String> defs = readMap(buf, FriendlyByteBuf::readUtf);
        Map<ResourceLocation, String> chapters = readMap(buf, FriendlyByteBuf::readUtf);
        Map<ResourceLocation, CompoundTag> data = readMap(buf, FriendlyByteBuf::readNbt);
        List<ResourceLocation> advancements = readList(buf);
        return new QuestSyncPacket(defs, chapters, data, advancements);
    }

    private static <V> Map<ResourceLocation, V> readMap(FriendlyByteBuf buf, Function<FriendlyByteBuf, V> valueReader) {
        Map<ResourceLocation, V> map = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            map.put(buf.readResourceLocation(), valueReader.apply(buf));
        }
        return map;
    }

    private static <V> void writeMap(FriendlyByteBuf buf, Map<ResourceLocation, V> map, BiConsumer<FriendlyByteBuf, V> valueWriter) {
        buf.writeVarInt(map.size());
        for (Map.Entry<ResourceLocation, V> entry : map.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            valueWriter.accept(buf, entry.getValue());
        }
    }

    private static List<ResourceLocation> readList(FriendlyByteBuf buf) {
        List<ResourceLocation> list = new ArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            list.add(buf.readResourceLocation());
        }
        return list;
    }

    private static void writeList(FriendlyByteBuf buf, List<ResourceLocation> list) {
        buf.writeVarInt(list.size());
        for (ResourceLocation rl : list) {
            buf.writeResourceLocation(rl);
        }
    }

    public static void handle(QuestSyncPacket packet, IPacketContext ctx) {
        ClientPacketHandler.handle(packet, ctx);
    }

    public void encode(FriendlyByteBuf buf) {
        writeMap(buf, this.definitions, FriendlyByteBuf::writeUtf);
        writeMap(buf, this.chapterDefinitions, FriendlyByteBuf::writeUtf);
        writeMap(buf, this.data, FriendlyByteBuf::writeNbt);
        writeList(buf, this.advancements);
    }
}
package org.infernalstudios.questlog.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.infernalstudios.questlog.Questlog;
import org.jetbrains.annotations.NotNull;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import org.infernalstudios.questlog.network.ClientPacketHandler;
import org.infernalstudios.questlog.network.IPacketContext;
*///?}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//? if <1.21 {
/*import java.util.function.BiConsumer;
import java.util.function.Function;
*///?}

//? if >=1.21 {
public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, String> chapterDefinitions,
                              Map<ResourceLocation, CompoundTag> data,
                              List<ResourceLocation> advancements) implements CustomPacketPayload {
//?} else {
/*public record QuestSyncPacket(Map<ResourceLocation, String> definitions,
                              Map<ResourceLocation, String> chapterDefinitions,
                              Map<ResourceLocation, CompoundTag> data,
                              List<ResourceLocation> advancements) {
*///?}
    //? if >=1.21 {
    public static final Type<QuestSyncPacket> TYPE = new Type<>(Questlog.id("sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull QuestSyncPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            Map<ResourceLocation, String> defs = readMap(buf, ByteBufCodecs.STRING_UTF8);
            Map<ResourceLocation, String> chapters = readMap(buf, ByteBufCodecs.STRING_UTF8);
            Map<ResourceLocation, CompoundTag> data = readMap(buf, ByteBufCodecs.COMPOUND_TAG);
            List<ResourceLocation> advancements = readList(buf);
            return new QuestSyncPacket(defs, chapters, data, advancements);
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, QuestSyncPacket packet) {
            writeMap(buf, packet.definitions(), ByteBufCodecs.STRING_UTF8);
            writeMap(buf, packet.chapterDefinitions(), ByteBufCodecs.STRING_UTF8);
            writeMap(buf, packet.data(), ByteBufCodecs.COMPOUND_TAG);
            writeList(buf, packet.advancements());
        }

        private List<ResourceLocation> readList(RegistryFriendlyByteBuf buf) {
            List<ResourceLocation> list = new ArrayList<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                list.add(ResourceLocation.STREAM_CODEC.decode(buf));
            }
            return list;
        }

        private void writeList(RegistryFriendlyByteBuf buf, List<ResourceLocation> list) {
            buf.writeVarInt(list.size());
            for (ResourceLocation rl : list) {
                ResourceLocation.STREAM_CODEC.encode(buf, rl);
            }
        }

        private <V> Map<ResourceLocation, V> readMap(RegistryFriendlyByteBuf buf, StreamCodec<? super RegistryFriendlyByteBuf, V> valueCodec) {
            Map<ResourceLocation, V> map = new HashMap<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                map.put(ResourceLocation.STREAM_CODEC.decode(buf), valueCodec.decode(buf));
            }
            return map;
        }

        private <V> void writeMap(RegistryFriendlyByteBuf buf, Map<ResourceLocation, V> map, StreamCodec<? super RegistryFriendlyByteBuf, V> valueCodec) {
            buf.writeVarInt(map.size());
            for (Map.Entry<ResourceLocation, V> entry : map.entrySet()) {
                ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
                valueCodec.encode(buf, entry.getValue());
            }
        }
    };
    //?} else {
    /*public static final IPacketContext.Direction DIRECTION = IPacketContext.Direction.SERVER_TO_CLIENT;

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
    *///?}

    //? if >=1.21 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}

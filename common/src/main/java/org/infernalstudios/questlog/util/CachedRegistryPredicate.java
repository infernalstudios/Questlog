package org.infernalstudios.questlog.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.function.BiPredicate;

public class CachedRegistryPredicate<T> {

    private final boolean isTag;

    private final CachedValue<T> entry;
    private final CachedValue<TagKey<T>> tag;

    private final BiPredicate<T, T> test;
    private final BiPredicate<TagKey<T>, T> tagTest;

    public CachedRegistryPredicate(String location, Registry<T> registry, BiPredicate<T, T> test, BiPredicate<TagKey<T>, T> tagTest) {
        if (location.startsWith("#")) {
            this.isTag = true;
            location = location.substring(1);
        } else {
            this.isTag = false;
        }

        ResourceLocation key = ResourceLocation.parse(location);

        this.entry = new CachedValue<>(() -> registry.get(key));
        this.tag = new CachedValue<>(() -> TagKey.create(registry.key(), key));

        this.tagTest = tagTest;
        this.test = test;
    }

    public static CachedRegistryPredicate<Item> item(String location) {
        return new CachedRegistryPredicate<>(location, BuiltInRegistries.ITEM, Objects::equals, (tag, item) -> item.getDefaultInstance().is(tag));
    }

    public static CachedRegistryPredicate<Block> block(String location) {
        return new CachedRegistryPredicate<>(location, BuiltInRegistries.BLOCK, Objects::equals, (tag, block) -> block.defaultBlockState().is(tag));
    }

    public boolean test(T value) {
        if (this.isTag) {
            return this.tagTest.test(this.tag.get(), value);
        } else {
            return this.test.test(this.entry.get(), value);
        }
    }
}

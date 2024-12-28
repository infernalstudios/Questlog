package org.infernalstudios.questlog.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;

public class CachedRegistryPredicate<T> {

  private final boolean isTag;

  private final CachedValue<T> entry;
  private final CachedValue<TagKey<T>> tag;

  private final BiPredicate<T, T> test;
  private final BiPredicate<TagKey<T>, T> tagTest;

  public CachedRegistryPredicate(String location, IForgeRegistry<T> registry, BiPredicate<T, T> test, BiPredicate<TagKey<T>, T> tagTest) {
    if (location.startsWith("#")) {
      this.isTag = true;
      location = location.substring(1);

      if (registry.tags() == null) {
        throw new IllegalStateException("Registry " + registry.getRegistryName() + " does not support tags");
      }
    } else {
      this.isTag = false;
    }

    ResourceLocation key = new ResourceLocation(location);

    this.entry = new CachedValue<>(() -> registry.getValue(key));
    this.tag = new CachedValue<>(() ->
      Objects.requireNonNull(registry.tags(), "Registry " + registry.getRegistryName() + " does not support tags").createTagKey(key)
    );

    this.tagTest = tagTest;
    this.test = test;
  }

  public boolean test(T value) {
    if (this.isTag) {
      return this.tagTest.test(this.tag.get(), value);
    } else {
      return this.test.test(this.entry.get(), value);
    }
  }
}

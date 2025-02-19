package org.infernalstudios.questlog.util;

import java.util.function.Supplier;

public class CachedValue<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private T value;

  public CachedValue(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (this.value == null) {
      this.value = this.supplier.get();
    }

    return this.value;
  }
}

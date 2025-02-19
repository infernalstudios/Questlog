package org.infernalstudios.questlog.event;

import net.jodah.typetools.TypeResolver;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QLEvent;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple event bus for Questlog events.
 * <p>
 * This class is basically a stripped down reimplementation of Forge's event bus.
 */
public class QuestlogEventBus {
  private final Map<Class<?>, List<Consumer<? extends QLEvent>>> listeners = new HashMap<>();

  public <T extends QLEvent> void addListener(Consumer<T> listener) {
    //noinspection unchecked
    Class<T> eventClass = (Class<T>) TypeResolver.resolveRawArgument(Consumer.class, listener.getClass());
    if (eventClass != null && ((Class<?>) eventClass) != TypeResolver.Unknown.class) {
      addListener(eventClass, listener);
    } else {
      throw new IllegalArgumentException("Could not resolve event class for listener " + listener);
    }
  }

  public <T extends QLEvent> void addListener(Class<T> eventClass, Consumer<T> listener) {
    if (eventClass.isAssignableFrom(QLEvent.class)) {
      Questlog.LOGGER.warn("Registering an event of class {} which is not a subclass of QLEvent", eventClass);
    }

    List<Consumer<? extends QLEvent>> listeners = this.listeners.computeIfAbsent(eventClass, k -> new ArrayList<>(1));
    listeners.add(listener);
  }

  public <T extends QLEvent> void post(T event) {
    List<Consumer<? extends QLEvent>> listeners = this.listeners.get(event.getClass());
    if (listeners != null) {
      for (Consumer<? extends QLEvent> listener : listeners) {
        //noinspection unchecked
        ((Consumer<T>) listener).accept(event);
      }
    }
  }

  public void removeAllListeners() {
    this.listeners.clear();
  }
}


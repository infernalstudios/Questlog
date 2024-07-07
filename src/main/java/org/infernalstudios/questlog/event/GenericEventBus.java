package org.infernalstudios.questlog.event;

import net.jodah.typetools.TypeResolver;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.infernalstudios.questlog.Questlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

// This class is a generic event bus that allows for the registration of dynamic listeners that are resolved at runtime.
// This is used because Forge's event bus sometimes does not work as expected with Objective::registerEventListeners.
public class GenericEventBus {
  private final Marker GENERIC_EVENTBUS = MarkerManager.getMarker("GENERIC_EVENTBUS");

  private final Map<Class<? extends Event>, List<Consumer<Event>>> listeners = new HashMap<>();

  public GenericEventBus(IEventBus parent) {
    parent.addListener(EventPriority.LOWEST, this::genericListener);
  }

  private void genericListener(Event event) {
    Class<? extends Event> eventClass = event.getClass();
    listeners.getOrDefault(eventClass, new ArrayList<>()).forEach(listener -> listener.accept(event));
  }

  @SuppressWarnings("unchecked")
  private <T extends Event> Class<T> getEventClass(Consumer<T> consumer) {
    final Class<T> eventClass = (Class<T>) TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());

    if ((Class<?>) eventClass == TypeResolver.Unknown.class) {
      Questlog.LOGGER.error(GENERIC_EVENTBUS, "Failed to resolve handler for \"{}\"", consumer.toString());
      throw new IllegalStateException("Failed to resolve consumer event type: " + consumer.toString());
    }

    return eventClass;
  }

  @SuppressWarnings("unchecked")
  public <T extends Event> void addListener(Consumer<T> listener) {
    Class<T> eventClass = getEventClass(listener);
    Questlog.LOGGER.trace(GENERIC_EVENTBUS, "Adding generically resolved listener for {}", eventClass.getName());
    listeners.computeIfAbsent(eventClass, c -> new ArrayList<>()).add(event -> listener.accept((T) event));
  }

  public void removeAllListeners() {
    listeners.clear();
  }
}

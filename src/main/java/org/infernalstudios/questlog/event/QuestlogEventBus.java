package org.infernalstudios.questlog.event;

import net.jodah.typetools.TypeResolver;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.event.events.QuestEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple event bus for Questlog events.
 * <p>
 * This class is basically a stripped down reimplementation of NeoForge's event bus.
 */
public class QuestlogEventBus {
    private final Map<Class<?>, List<Consumer<? extends QuestEvent>>> listeners = new HashMap<>();

    public <T extends QuestEvent> void addListener(Consumer<T> listener) {
        //noinspection unchecked
        Class<T> eventClass = (Class<T>) TypeResolver.resolveRawArgument(Consumer.class, listener.getClass());
        if (eventClass != null && ((Class<?>) eventClass) != TypeResolver.Unknown.class) {
            addListener(eventClass, listener);
        } else {
            throw new IllegalArgumentException("Could not resolve event class for listener " + listener);
        }
    }

    public <T extends QuestEvent> void addListener(Class<T> eventClass, Consumer<T> listener) {
        if (!QuestEvent.class.isAssignableFrom(eventClass)) {
            Questlog.LOGGER.warn("Registering an event of class {} which is not a subclass of QuestEvent", eventClass);
        }

        List<Consumer<? extends QuestEvent>> listeners = this.listeners.computeIfAbsent(eventClass, k -> new ArrayList<>(1));
        listeners.add(listener);
    }

    public <T extends QuestEvent> void post(T event) {
        List<Consumer<? extends QuestEvent>> listeners = this.listeners.get(event.getClass());
        if (listeners != null) {
            for (Consumer<? extends QuestEvent> listener : listeners) {
                //noinspection unchecked
                ((Consumer<T>) listener).accept(event);
            }
        }
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }
}


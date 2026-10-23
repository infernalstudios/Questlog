package org.infernalstudios.questlog.core.quests.objectives;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerBlockEvent;
import com.evandev.triggers.event.events.TriggerEntityEvent;
import com.evandev.triggers.event.events.TriggerEvent;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import org.infernalstudios.questlog.core.quests.Quest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Routes Triggers events to quest objectives.
 */
public class ObjectiveListeners {
    private static final Map<Class<?>, List<Listener<?>>> LISTENERS = new ConcurrentHashMap<>();

    public static void init() {
        for (Class<?> group : List.of(TriggerBlockEvent.class, TriggerEntityEvent.class, TriggerPlayerEvent.class)) {
            for (Class<?> eventClass : group.getDeclaredClasses()) {
                if (TriggerEvent.class.isAssignableFrom(eventClass)) {
                    LISTENERS.computeIfAbsent(eventClass, ObjectiveListeners::forward);
                }
            }
        }
    }

    public static <T extends TriggerEvent> void add(Objective objective, Class<T> eventClass, Consumer<T> listener) {
        LISTENERS.computeIfAbsent(eventClass, ObjectiveListeners::forward).add(new Listener<>(objective, listener));
    }

    /**
     * Removes the listeners of every objective belonging to the given quests.
     */
    public static void removeAll(Collection<Quest> quests) {
        if (quests.isEmpty()) return;
        Set<Quest> removed = Set.copyOf(quests);
        for (List<Listener<?>> listeners : LISTENERS.values()) {
            listeners.removeIf(listener -> removed.contains(listener.objective().getParent()));
        }
    }

    public static void clear() {
        LISTENERS.values().forEach(List::clear);
    }

    private static List<Listener<?>> forward(Class<?> eventClass) {
        Triggers.EVENTS.addListener(eventClass.asSubclass(TriggerEvent.class), ObjectiveListeners::post);
        return new CopyOnWriteArrayList<>();
    }

    private static <T extends TriggerEvent> void post(T event) {
        List<Listener<?>> listeners = LISTENERS.get(event.getClass());
        if (listeners != null) {
            for (Listener<?> listener : listeners) {
                //noinspection unchecked
                ((Consumer<T>) listener.consumer()).accept(event);
            }
        }
    }

    private record Listener<T extends TriggerEvent>(Objective objective, Consumer<T> consumer) {
    }
}

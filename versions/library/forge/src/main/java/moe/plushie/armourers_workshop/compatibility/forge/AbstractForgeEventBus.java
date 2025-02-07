package moe.plushie.armourers_workshop.compatibility.forge;

import com.google.common.collect.ImmutableMap;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.registry.IEventHandler;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.IModBusEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Available("[1.21, )")
public class AbstractForgeEventBus {

    private static final ImmutableMap<IEventHandler.Priority, EventPriority> CONVERTER = ImmutableMap.<IEventHandler.Priority, EventPriority>builder()
            .put(IEventHandler.Priority.HIGHEST, EventPriority.HIGHEST)
            .put(IEventHandler.Priority.HIGH, EventPriority.HIGH)
            .put(IEventHandler.Priority.NORMAL, EventPriority.NORMAL)
            .put(IEventHandler.Priority.LOW, EventPriority.LOW)
            .put(IEventHandler.Priority.LOWEST, EventPriority.LOWEST)
            .build();

    private static final HashMap<Object, ArrayList<?>> LISTENERS = new HashMap<>();

    public static <E extends Event> void observer(Class<E> eventType, Consumer<E> handler) {
        observer(eventType, EventPriority.NORMAL, false, handler, event -> event);
    }

    public static <E extends Event, T> void observer(Class<E> eventType, EventPriority priority, boolean receiveCancelled, Consumer<T> handler, Function<E, T> transform) {
        Object key = Triple.of(eventType, priority, receiveCancelled);
        ArrayList<Consumer<T>> handlers = Objects.unsafeCast(LISTENERS.computeIfAbsent(key, key1 -> {
            ArrayList<Consumer<T>> queue = new ArrayList<>();
            Consumer<E> listener = event -> queue.forEach(element -> element.accept(transform.apply(event)));
            if (IModBusEvent.class.isAssignableFrom(eventType)) {
                AbstractForgeInitializer.getModEventBus().addListener(priority, receiveCancelled, eventType, listener);
            } else {
                AbstractForgeInitializer.getEventBus().addListener(priority, receiveCancelled, eventType, listener);
            }
            return queue;
        }));
        handlers.add(handler);
    }

    public static <E extends Event> IEventHandler<E> create(Class<E> eventType) {
        return (priority, receiveCancelled, handler) -> observer(eventType, CONVERTER.getOrDefault(priority, EventPriority.NORMAL), receiveCancelled, handler, event -> event);
    }
}

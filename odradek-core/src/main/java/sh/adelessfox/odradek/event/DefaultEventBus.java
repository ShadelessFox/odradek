package sh.adelessfox.odradek.event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public final class DefaultEventBus implements EventBus {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();
    private final List<Event.Sticky> stickyEvents = new CopyOnWriteArrayList<>();

    @Override
    public <T extends Event> void subscribe(Class<? extends T> topic, Consumer<T> subscriber) {
        subscribers
            .computeIfAbsent(topic, _ -> new CopyOnWriteArraySet<>())
            .add(subscriber);

        for (Event.Sticky event : stickyEvents) {
            if (topic.isInstance(event)) {
                publish(event, subscriber);
            }
        }
    }

    @Override
    public <T extends Event> void unsubscribe(Consumer<T> subscriber) {
        subscribers.values().forEach(subscribers -> subscribers.remove(subscriber));
    }

    @Override
    public <T extends Event> void unsubscribe(Class<? extends T> topic, Consumer<T> subscriber) {
        subscribers.entrySet().stream()
            .filter(entry -> topic.isAssignableFrom(entry.getKey()))
            .forEach(entry -> entry.getValue().remove(subscriber));
    }

    @Override
    public <T extends Event> void publish(T event) {
        if (event instanceof Event.Sticky sticky) {
            stickyEvents.add(sticky);
        }

        subscribers.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(event))
            .flatMap(entry -> entry.getValue().stream())
            .forEach(subscriber -> publish(event, subscriber));
    }

    private <T extends Event> void publish(T event, Consumer<?> subscriber) {
        @SuppressWarnings("unchecked")
        var subscriber1 = (Consumer<T>) subscriber;
        subscriber1.accept(event);
    }
}

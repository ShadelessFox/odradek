package sh.adelessfox.odradek.event;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public final class DefaultEventBus implements EventBus {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> void subscribe(Class<? extends T> topic, Consumer<T> subscriber) {
        subscribers
            .computeIfAbsent(topic, _ -> new CopyOnWriteArraySet<>())
            .add(subscriber);
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> void publish(T event) {
        subscribers.entrySet().stream()
            .filter(entry -> entry.getKey().isInstance(event))
            .flatMap(entry -> entry.getValue().stream())
            .forEach(subscriber -> publish(event, (Consumer<T>) subscriber));
    }

    private <T extends Event> void publish(T event, Consumer<? super T> subscriber) {
        subscriber.accept(event);
    }
}

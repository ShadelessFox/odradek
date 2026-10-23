package sh.adelessfox.odradek.event;

import java.util.function.Consumer;

/**
 * Represents a bus for publishing and subscribing to events.
 *
 * @see Event
 */
public interface EventBus {
    interface Subscription {
        /** Removes the listener from this subscription's topic. */
        void unsubscribe();
    }

    /**
     * Subscribes a subscriber to a specific topic.
     * <p>
     * When an event of the specified type is published, the subscriber will be notified.
     *
     * @param topic      the class of the event to subscribe to
     * @param subscriber the subscriber that will receive events of the specified type
     * @param <T>        the type of the event
     */
    <T extends Event> Subscription subscribe(Class<? extends T> topic, Consumer<T> subscriber);

    /**
     * Publishes an event to all subscribers of the event's type.
     * <p>
     * If the event implements the {@link Event.Sticky} interface, it will be retained
     * in the event bus and delivered to new subscribers immediately upon subscription.
     *
     * @param event the event to publish
     * @param <T>   the type of the event
     */
    <T extends Event> void publish(T event);
}

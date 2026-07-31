package sh.adelessfox.odradek.event;

public interface Event {
    /**
     * Marker interface for events that should be sticky.
     * Sticky events are not removed from the event bus after being published,
     * and will be delivered to new subscribers immediately upon subscription.
     *
     * <h4>Usage notice</h3>
     * Care must be taken when using sticky events, as they can lead to memory leaks
     * because they are retained in the event bus for its lifetime.
     */
    interface Sticky extends Event {
    }
}

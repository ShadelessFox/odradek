package sh.adelessfox.odradek.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultEventBusTest {
    @Test
    void publishesToSubscribersOfTheExactTopic() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Message>();
        bus.subscribe(Message.class, received::add);

        var event = new Message("hello");
        bus.publish(event);

        assertEquals(List.of(event), received);
    }

    @Test
    void publishesToSubscribersOfParentTopics() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Event>();
        bus.subscribe(Event.class, received::add);
        bus.subscribe(ParentEvent.class, received::add);

        var event = new ChildEvent(42);
        bus.publish(event);

        assertEquals(2, received.size());
        assertTrue(received.stream().allMatch(event::equals));
    }

    @Test
    void doesNotPublishToUnrelatedTopics() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Message>();
        bus.subscribe(Message.class, received::add);

        bus.publish(new ChildEvent(42));

        assertTrue(received.isEmpty());
    }

    @Test
    void ignoresDuplicateSubscriptionsToTheSameTopic() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Message>();
        Consumer<Message> subscriber = received::add;
        bus.subscribe(Message.class, subscriber);
        bus.subscribe(Message.class, subscriber);

        bus.publish(new Message("hello"));

        assertEquals(1, received.size());
    }

    @Test
    void unsubscribesFromAllTopics() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Event>();
        Consumer<Event> subscriber = received::add;
        bus.subscribe(Event.class, subscriber);
        bus.subscribe(ParentEvent.class, subscriber);

        bus.unsubscribe(subscriber);
        bus.publish(new ChildEvent(42));

        assertTrue(received.isEmpty());
    }

    @Test
    void unsubscribesFromTopicAndItsSubtopics() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<Event>();
        Consumer<Event> subscriber = received::add;
        bus.subscribe(Event.class, subscriber);
        bus.subscribe(ParentEvent.class, subscriber);
        bus.subscribe(ChildEvent.class, subscriber);

        bus.unsubscribe(ParentEvent.class, subscriber);
        bus.publish(new ChildEvent(42));

        assertEquals(1, received.size());
    }

    @Test
    void deliversStickyEventsToLaterSubscribers() {
        var bus = new DefaultEventBus();
        var first = new StickyMessage("first");
        var second = new StickyMessage("second");
        bus.publish(first);
        bus.publish(second);

        var received = new ArrayList<StickyMessage>();
        bus.subscribe(StickyMessage.class, received::add);

        assertEquals(List.of(first, second), received);
    }

    @Test
    void deliversStickyEventsThroughParentTopics() {
        var bus = new DefaultEventBus();
        var event = new StickyChildEvent(42);
        bus.publish(event);

        var received = new ArrayList<ParentEvent>();
        bus.subscribe(ParentEvent.class, received::add);

        assertEquals(List.of(event), received);
    }

    @Test
    void continuesDeliveringEventsAfterStickyReplay() {
        var bus = new DefaultEventBus();
        var sticky = new StickyMessage("sticky");
        bus.publish(sticky);

        var received = new ArrayList<Event>();
        bus.subscribe(Event.class, received::add);
        var regular = new Message("regular");
        bus.publish(regular);

        assertEquals(List.of(sticky, regular), received);
    }

    private interface ParentEvent extends Event {
    }

    private record Message(String value) implements Event {
    }

    private record ChildEvent(int value) implements ParentEvent {
    }

    private record StickyMessage(String value) implements Event.Sticky {
    }

    private record StickyChildEvent(int value) implements ParentEvent, Event.Sticky {
    }
}

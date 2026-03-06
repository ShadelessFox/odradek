package sh.adelessfox.odradek.app.ui.component.graph;

import sh.adelessfox.odradek.event.Event;
import sh.adelessfox.odradek.game.ObjectId;

public sealed interface GraphViewEvent extends Event {
    record UpdateFilter(String query, boolean matchCase, boolean matchWholeWord) implements GraphViewEvent {
    }

    record ShowObject(ObjectId objectId) implements GraphViewEvent {
    }

    record ShowLinks(ObjectId objectId) implements GraphViewEvent {
    }
}

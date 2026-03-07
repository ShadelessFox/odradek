package sh.adelessfox.odradek.app.ui.component.main;

import sh.adelessfox.odradek.event.Event;
import sh.adelessfox.odradek.game.ObjectId;

public sealed interface MainEvent extends Event {
    record ShowPanel(String id) implements MainEvent {
    }

    record ShowObject(ObjectId objectId) implements MainEvent {
    }

    record ShowLinks(ObjectId objectId) implements MainEvent {
    }
}

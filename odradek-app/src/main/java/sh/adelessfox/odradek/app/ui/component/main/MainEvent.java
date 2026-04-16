package sh.adelessfox.odradek.app.ui.component.main;

import sh.adelessfox.odradek.event.Event;
import sh.adelessfox.odradek.game.decima.ObjectId;

public sealed interface MainEvent extends Event {
    record ShowPanel(String id) implements MainEvent {
    }

    record ShowObject(ObjectId objectId) implements MainEvent {
    }

    record ShowUsages(ObjectId objectId) implements MainEvent {
    }
}

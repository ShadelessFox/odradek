package sh.adelessfox.odradek.app.ui.component.links;

import sh.adelessfox.odradek.event.Event;
import sh.adelessfox.odradek.game.ObjectId;

public sealed interface LinkEvent extends Event {
    record ShowFor(ObjectId objectId) implements LinkEvent {
    }
}

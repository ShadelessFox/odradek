package sh.adelessfox.odradek.app.ui.component.graph;

import sh.adelessfox.odradek.event.Event;

public sealed interface GraphToolPanelEvent extends Event {
    record UpdateFilter(String query, boolean matchCase, boolean matchWholeWord) implements GraphToolPanelEvent {
    }
}

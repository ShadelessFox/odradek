package sh.adelessfox.odradek.app.ui.component.graph;

import sh.adelessfox.odradek.event.Event;

public sealed interface GraphViewEvent extends Event {
    record UpdateFilter(String query, boolean matchCase, boolean matchWholeWord) implements GraphViewEvent {}

    record ShowObject(int groupId, int objectIndex) implements GraphViewEvent {}
}

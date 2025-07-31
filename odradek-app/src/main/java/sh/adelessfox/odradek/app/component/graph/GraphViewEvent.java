package sh.adelessfox.odradek.app.component.graph;

import sh.adelessfox.odradek.event.Event;

public sealed interface GraphViewEvent extends Event {
    record FilterChanged(String query, boolean matchCase, boolean matchWholeWord) implements GraphViewEvent {}

    record ObjectSelected(int groupId, int objectIndex) implements GraphViewEvent {}
}

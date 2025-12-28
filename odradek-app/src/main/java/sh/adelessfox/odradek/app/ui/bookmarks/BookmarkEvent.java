package sh.adelessfox.odradek.app.ui.bookmarks;

import sh.adelessfox.odradek.event.Event;

public sealed interface BookmarkEvent extends Event {
    record BookmarkAdded(Bookmark bookmark) implements BookmarkEvent {
    }

    record BookmarkUpdated(Bookmark bookmark) implements BookmarkEvent {
    }

    record BookmarkRemoved(Bookmark bookmark) implements BookmarkEvent {
    }
}

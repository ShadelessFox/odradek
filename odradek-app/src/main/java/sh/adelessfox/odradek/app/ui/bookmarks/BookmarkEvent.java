package sh.adelessfox.odradek.app.ui.bookmarks;

import sh.adelessfox.odradek.event.Event;

public sealed interface BookmarkEvent extends Event {
    record BookmarkAdded(Bookmark bookmark) implements BookmarkEvent {
    }

    record BookmarkUpdated(Bookmark bookmark) implements BookmarkEvent {
    }

    record BookmarkRemoved(Bookmark bookmark) implements BookmarkEvent {
    }

    record BookmarkMoved(Bookmark bookmark, FolderId oldFolder, FolderId newFolder) implements BookmarkEvent {
    }

    record FolderAdded(Folder folder) implements BookmarkEvent {
    }

    record FolderUpdated(Folder folder) implements BookmarkEvent {
    }

    record FolderRemoved(Folder folder) implements BookmarkEvent {
    }

    record FolderMoved(Folder folder, FolderId oldParent, FolderId newParent) implements BookmarkEvent {
    }
}

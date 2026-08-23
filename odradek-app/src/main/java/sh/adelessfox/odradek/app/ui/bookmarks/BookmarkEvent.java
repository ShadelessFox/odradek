package sh.adelessfox.odradek.app.ui.bookmarks;

import sh.adelessfox.odradek.event.Event;

public sealed interface BookmarkEvent extends Event {
    record BookmarkAdded(Bookmark bookmark, FolderId parent) implements BookmarkEvent {
    }

    record BookmarkUpdated(Bookmark bookmark, FolderId parent) implements BookmarkEvent {
    }

    record BookmarkRemoved(Bookmark bookmark, FolderId parent) implements BookmarkEvent {
    }

    record BookmarkMoved(Bookmark bookmark, FolderId oldFolder, FolderId newFolder) implements BookmarkEvent {
    }

    record FolderAdded(Folder folder, FolderId parent) implements BookmarkEvent {
    }

    record FolderUpdated(Folder folder, FolderId parent) implements BookmarkEvent {
    }

    record FolderRemoved(Folder folder, FolderId parent) implements BookmarkEvent {
    }

    record FolderMoved(Folder folder, FolderId oldParent, FolderId newParent) implements BookmarkEvent {
    }
}

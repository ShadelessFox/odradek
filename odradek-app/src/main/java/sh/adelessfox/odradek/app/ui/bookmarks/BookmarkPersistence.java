package sh.adelessfox.odradek.app.ui.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkSettings.BookmarkState;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads bookmarks once and saves them independently of the tool panel.
 * Construct once for an empty repository using already-loaded settings, before creating the UI.
 * The save subscription retains this service for the application event bus's lifetime.
 */
public final class BookmarkPersistence {
    private final Bookmarks bookmarks;
    private final Settings settings;

    public BookmarkPersistence(Bookmarks bookmarks, Settings settings, EventBus events) {
        this.bookmarks = bookmarks;
        this.settings = settings;

        var state = settings.get(BookmarkSettings.BOOKMARKS).value();
        deserialize(bookmarks.rootFolderId(), state);
        events.subscribe(SettingsEvent.BeforeSave.class, event -> {
            if (event.settings() == settings) {
                save();
            }
        });
    }

    private void deserialize(FolderId folderId, List<BookmarkState> children) {
        for (var child : children) {
            switch (child) {
                case BookmarkState.Bookmark bookmark ->
                    bookmarks.create(folderId, bookmark.key(), bookmark.name());
                case BookmarkState.Folder folder ->
                    deserialize(bookmarks.createFolder(folderId, folder.name()), folder.children());
            }
        }
    }

    private void save() {
        List<BookmarkState> state;
        // Autosave may run off the EDT. Take one consistent snapshot, then release the
        // repository monitor before acquiring settings locks to write it.
        synchronized (bookmarks) {
            state = serialize(bookmarks.rootFolderId());
        }
        settings.get(BookmarkSettings.BOOKMARKS).set(state);
    }

    private List<BookmarkState> serialize(FolderId folderId) {
        var children = new ArrayList<BookmarkState>();
        for (var folder : bookmarks.getAllFoldersInFolder(folderId)) {
            children.add(new BookmarkState.Folder(folder.name(), serialize(folder.id())));
        }
        for (var bookmark : bookmarks.getAllInFolder(folderId)) {
            children.add(new BookmarkState.Bookmark(bookmark.key(), bookmark.name()));
        }
        return List.copyOf(children);
    }
}

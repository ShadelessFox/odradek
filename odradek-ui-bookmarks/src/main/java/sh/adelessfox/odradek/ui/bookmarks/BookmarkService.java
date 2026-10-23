package sh.adelessfox.odradek.ui.bookmarks;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.data.DataKey;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.service.ApplicationService;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.ui.bookmarks.BookmarkSettings.BookmarkState;
import sh.adelessfox.odradek.ui.data.DataKeys;

import java.util.ArrayList;
import java.util.List;

public final class BookmarkService implements ApplicationService {
    public static final class Provider implements ApplicationService.Provider {
        private static final DataKey<Settings> SETTINGS = DataKey.of(Settings.class);

        @Override
        public ApplicationService create(DataContext context) {
            var service = new BookmarkService(context.require(SETTINGS), context.require(DataKeys.EVENT_BUS));
            context.put(BookmarkDataKeys.BOOKMARKS, service.bookmarks);
            return service;
        }
    }

    private final BookmarkRepository bookmarks;
    private final Settings settings;
    private final EventBus.Subscription subscription;

    private BookmarkService(Settings settings, EventBus events) {
        this.bookmarks = new BookmarkRepository(events);
        this.settings = settings;

        load(bookmarks.rootFolderId(), settings.get(BookmarkSettings.BOOKMARKS).value());
        subscription = events.subscribe(SettingsEvent.BeforeSave.class, this::save);
    }

    @Override
    public void dispose() {
        subscription.unsubscribe();
    }

    private void load(FolderId folderId, List<BookmarkState> children) {
        for (var child : children) {
            switch (child) {
                case BookmarkState.Bookmark b -> bookmarks.create(folderId, b.key(), b.name());
                case BookmarkState.Folder f -> load(bookmarks.createFolder(folderId, f.name()), f.children());
            }
        }
    }

    private void save(SettingsEvent.BeforeSave event) {
        if (event.settings() == settings) {
            save();
        }
    }

    private void save() {
        List<BookmarkState> state;
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

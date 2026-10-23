package sh.adelessfox.odradek.app.ui.tools.bookmarks.data;

import sh.adelessfox.odradek.data.DataKey;

/** Context bindings owned by the bookmark feature. */
public final class BookmarkDataKeys {
    public static final DataKey<BookmarkRepository> BOOKMARKS = DataKey.of(BookmarkRepository.class);

    private BookmarkDataKeys() {
    }
}

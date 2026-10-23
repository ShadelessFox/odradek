package sh.adelessfox.odradek.ui.bookmarks;

import sh.adelessfox.odradek.data.DataKey;

/** Context bindings owned by the bookmark feature. */
public final class BookmarkDataKeys {
    public static final DataKey<BookmarkRepository> BOOKMARKS = DataKey.of(BookmarkRepository.class);

    private BookmarkDataKeys() {
    }
}

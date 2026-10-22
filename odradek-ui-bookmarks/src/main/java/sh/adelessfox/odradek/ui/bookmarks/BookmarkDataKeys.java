package sh.adelessfox.odradek.ui.bookmarks;

import sh.adelessfox.odradek.data.DataKey;

/** Context bindings owned by the bookmark feature. */
public final class BookmarkDataKeys {
    public static final DataKey<Bookmarks> BOOKMARKS = DataKey.of(Bookmarks.class);

    private BookmarkDataKeys() {
    }
}

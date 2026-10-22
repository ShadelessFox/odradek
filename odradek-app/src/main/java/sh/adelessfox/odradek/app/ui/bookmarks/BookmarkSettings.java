package sh.adelessfox.odradek.app.ui.bookmarks;

import com.google.gson.annotations.JsonAdapter;
import sh.adelessfox.odradek.app.ui.bookmarks.gson.BookmarkStateAdapter;
import sh.adelessfox.odradek.settings.SettingsKey;

import java.util.List;

public final class BookmarkSettings {
    public static final SettingsKey<List<BookmarkState>> BOOKMARKS =
        SettingsKey.listOf("bookmarks", BookmarkState.class);

    private BookmarkSettings() {
    }

    @JsonAdapter(BookmarkStateAdapter.class)
    public sealed interface BookmarkState {
        String name();

        record Bookmark(BookmarkKey key, String name) implements BookmarkState {
        }

        record Folder(String name, List<BookmarkState> children) implements BookmarkState {
        }
    }
}

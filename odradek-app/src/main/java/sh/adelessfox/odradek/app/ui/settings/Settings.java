package sh.adelessfox.odradek.app.ui.settings;

import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.game.ObjectId;

import java.util.List;

public final class Settings {
    private final Setting<List<ObjectId>> objects = new Setting<>();
    private final Setting<List<Bookmark>> bookmarks = new Setting<>();

    Settings() {
    }

    public Setting<List<ObjectId>> objects() {
        return objects;
    }

    public Setting<List<Bookmark>> bookmarks() {
        return bookmarks;
    }
}

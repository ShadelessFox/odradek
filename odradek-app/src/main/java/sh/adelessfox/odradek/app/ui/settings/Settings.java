package sh.adelessfox.odradek.app.ui.settings;

import com.google.gson.annotations.JsonAdapter;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.app.ui.settings.gson.EditorStateAdapter;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer.Orientation;

import java.util.List;

public final class Settings {
    private final Setting<WindowState> window = new Setting<>();
    private final Setting<EditorState> editors = new Setting<>();
    private final Setting<List<Bookmark>> bookmarks = new Setting<>();

    Settings() {
    }

    public Setting<WindowState> window() {
        return window;
    }

    public Setting<EditorState> editors() {
        return editors;
    }

    public Setting<List<Bookmark>> bookmarks() {
        return bookmarks;
    }

    public record WindowState(int x, int y, int width, int height, boolean maximized) {
    }

    @JsonAdapter(value = EditorStateAdapter.class)
    public sealed interface EditorState {
        record Split(
            EditorState left,
            EditorState right,
            Orientation orientation,
            double proportion
        ) implements EditorState {
        }

        record Leaf(
            List<ObjectId> objects,
            int selection
        ) implements EditorState {
        }
    }
}

package sh.adelessfox.odradek.app.ui.settings;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.annotations.JsonAdapter;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.app.ui.settings.gson.EditorStateAdapter;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer.Orientation;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;

public final class Settings {
    private final Setting<WindowState> window = new Setting<>();
    private final Setting<EditorState> editors = new Setting<>();
    private final Setting<List<Bookmark>> bookmarks = new Setting<>();
    private final Setting<Theme> theme = new Setting<>(Theme.LIGHT);

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

    public Setting<Theme> theme() {
        return theme;
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

    public enum Theme {
        LIGHT(FlatLightLaf::new),
        DARK(FlatDarkLaf::new);

        private final Supplier<LookAndFeel> factory;

        Theme(Supplier<LookAndFeel> factory) {
            this.factory = factory;
        }

        public LookAndFeel createLookAndFeel() {
            return factory.get();
        }

        @Override
        public String toString() {
            return switch (this) {
                case LIGHT -> "Light";
                case DARK -> "Dark";
            };
        }
    }
}

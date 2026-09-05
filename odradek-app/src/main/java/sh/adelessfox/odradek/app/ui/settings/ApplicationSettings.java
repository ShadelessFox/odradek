package sh.adelessfox.odradek.app.ui.settings;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.annotations.JsonAdapter;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.settings.gson.BookmarkStateAdapter;
import sh.adelessfox.odradek.app.ui.settings.gson.EditorStateAdapter;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.settings.SettingsKey;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer.Orientation;
import sh.adelessfox.odradek.ui.tools.ToolState;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;

public final class ApplicationSettings {
    public static final SettingsKey<WindowState> WINDOW =
        SettingsKey.of("window", WindowState.class, () -> null);

    public static final SettingsKey<ToolState> TOOLS =
        SettingsKey.of("tools", ToolState.class, () -> null);

    public static final SettingsKey<EditorState> EDITORS =
        SettingsKey.of("editors", EditorState.class, EditorState::empty);

    public static final SettingsKey<List<BookmarkState>> BOOKMARKS =
        SettingsKey.listOf("bookmarks", BookmarkState.class, List::of);

    public static final SettingsKey<Theme> THEME =
        SettingsKey.of("theme", Theme.class, () -> Theme.LIGHT);

    public static final SettingsKey<Boolean> SHOW_OBJECT_PREVIEW =
        SettingsKey.of("showObjectPreview", Boolean.class, () -> true);

    public static final SettingsKey<Boolean> SHOW_OBJECT_TYPE_INFORMATION =
        SettingsKey.of("showObjectTypeInformation", Boolean.class, () -> false);

    private ApplicationSettings() {
    }

    public record WindowState(int x, int y, int width, int height, boolean maximized) {
    }

    @JsonAdapter(EditorStateAdapter.class)
    public sealed interface EditorState {
        static EditorState empty() {
            return new Leaf(List.of(), 0);
        }

        record Split(
            EditorState left,
            EditorState right,
            Orientation orientation,
            double proportion
        ) implements EditorState {
        }

        record Leaf(List<ObjectId> objects, int selection) implements EditorState {
        }
    }

    @JsonAdapter(BookmarkStateAdapter.class)
    public sealed interface BookmarkState {
        String name();

        record Bookmark(BookmarkKey key, String name) implements BookmarkState {
        }

        record Folder(String name, List<BookmarkState> children) implements BookmarkState {
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

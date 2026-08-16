package sh.adelessfox.odradek.viewer.font;

import com.formdev.flatlaf.FlatClientProperties;
import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.util.Optional;

public record FontViewer(Font font) implements Viewer {
    public static final class Provider implements Viewer.Provider<Font> {
        @Override
        public Viewer create(Font object, Game game, Optional<?> selection) {
            return new FontViewer(object);
        }

        @Override
        public String name() {
            return "Font";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:edit");
        }
    }

    @Override
    public JComponent createComponent() {
        var gallery = new GlyphGallery(font);
        gallery.putClientProperty(FlatClientProperties.STYLE, "background: $Editor.background");

        // Is there a better way to set the fucking background?
        var viewport = new JViewport() {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(UIManager.getColor("Editor.background"));
            }
        };
        viewport.setView(gallery);

        var pane = new JScrollPane();
        pane.setViewport(viewport);

        return pane;
    }
}

package sh.adelessfox.odradek.viewer.font;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

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
        var onSizeChanged = new IntConsumer[1];

        var pane = createView(consumer -> onSizeChanged[0] = consumer);
        pane.putClientProperty(FlatClientProperties.STYLE, "border: 1,0,0,0, $Component.borderColor");

        var toolbar = createToolBar(onSizeChanged[0]);
        toolbar.putClientProperty(FlatClientProperties.STYLE, "background: @componentBackground");

        var panel = new JPanel();
        panel.putClientProperty(FlatClientProperties.STYLE, "background: @componentBackground");
        panel.setLayout(new MigLayout("ins 0,gap 0,wrap", "", "[][grow,fill]"));
        panel.add(toolbar);
        panel.add(pane, "grow, push");

        return panel;
    }

    private JToolBar createToolBar(IntConsumer onSizeChanged) {
        var sizeCombo = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72});
        sizeCombo.addActionListener(_ -> onSizeChanged.accept(sizeCombo.getItemAt(sizeCombo.getSelectedIndex())));
        sizeCombo.setSelectedItem(72);

        var toolbar = new JToolBar();
        toolbar.add(new JLabel(font.name()));
        toolbar.addSeparator();
        toolbar.add(new JLabel("Size: "));
        toolbar.add(sizeCombo);

        return toolbar;
    }

    private JComponent createView(Consumer<IntConsumer> onSizeChanged) {
        var gallery = new GlyphGallery(font, 72);
        gallery.putClientProperty(FlatClientProperties.STYLE, "background: $Editor.background");
        onSizeChanged.accept(gallery::setSize);

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

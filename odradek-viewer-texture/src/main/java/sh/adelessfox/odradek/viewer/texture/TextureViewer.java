package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.texture.view.Channel;
import sh.adelessfox.odradek.viewer.texture.view.ImagePanel;
import sh.adelessfox.odradek.viewer.texture.view.ImageView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;

public final class TextureViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<Texture> {
        @Override
        public Viewer create(Texture object, Game game) {
            return new TextureViewer(object);
        }

        @Override
        public String name() {
            return "Texture";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:image");
        }
    }

    private final Texture texture;

    // Animated texture handling
    private Timer timer;
    private int frame;
    private boolean animate = true;

    private TextureViewer(Texture texture) {
        this.texture = texture;
    }

    @Override
    public JComponent createComponent() {
        var surfaces = texture.surfaces().stream()
            .map(s -> s.convert(texture.format(), new Surface.Converter.AWT()))
            // vvv pick either the very first mip, or all frames if it's an animated texture
            .limit(texture.duration().isPresent() ? Long.MAX_VALUE : 1)
            .toList();

        var imageView = new ImageView();
        imageView.setImage(surfaces.getFirst());
        imageView.setZoomToFit(true);

        var imagePane = new ImagePanel(imageView);
        var imageToolbar = new JToolBar();

        populateChannelActions(imageToolbar, imageView);

        // To visually separate it from the toolbar
        imagePane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(imagePane, BorderLayout.CENTER);
        panel.add(imageToolbar, BorderLayout.NORTH);

        texture.duration().ifPresent(duration -> {
            var slider = new JSlider(0, surfaces.size() - 1);
            var animate = new JCheckBox("Animate", this.animate);

            imageToolbar.addSeparator();
            imageToolbar.add(animate);
            imageToolbar.add(slider);

            timer = new Timer(Math.toIntExact(duration.toMillis()), _ -> {
                imageView.setImage(surfaces.get(frame));
                slider.setValue(frame);
                frame = (frame + 1) % surfaces.size();
            });

            slider.addChangeListener(_ -> {
                frame = slider.getValue();
                imageView.setImage(surfaces.get(frame));
            });
            animate.addActionListener(_ -> {
                if (animate.isSelected()) {
                    play();
                    this.animate = true;
                } else {
                    stop();
                    this.animate = false;
                }
            });

            timer.start();
        });

        return panel;
    }

    @Override
    public void activate() {
        if (animate) {
            play();
        }
    }

    @Override
    public void deactivate() {
        stop();
    }

    private void play() {
        if (timer != null) {
            timer.start();
        }
    }

    private void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    private static void populateChannelActions(JToolBar toolBar, ImageView view) {
        var buttons = new ArrayList<ToggleChannelButton>();
        var update = (Runnable) () -> {
            var channels = buttons.stream()
                .filter(AbstractButton::isSelected)
                .map(ToggleChannelButton::getChannel)
                .collect(() -> EnumSet.noneOf(Channel.class), EnumSet::add, EnumSet::addAll);

            view.setChannels(channels);
        };

        var toggleChannel = (ActionListener) _ -> update.run();
        var selectChannel = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                for (JToggleButton button : buttons) {
                    button.setSelected(button == e.getSource());
                }
                update.run();
            }
        };

        for (Channel channel : Channel.values()) {
            var button = new ToggleChannelButton(channel);
            button.setSelected(true);
            button.addActionListener(toggleChannel);
            button.addMouseListener(selectChannel);

            toolBar.add(button);
            buttons.add(button);
        }
    }

    private static class ToggleChannelButton extends JToggleButton {
        private final Channel channel;

        ToggleChannelButton(Channel channel) {
            super(channel.getName());
            this.channel = channel;
        }

        Channel getChannel() {
            return channel;
        }
    }
}

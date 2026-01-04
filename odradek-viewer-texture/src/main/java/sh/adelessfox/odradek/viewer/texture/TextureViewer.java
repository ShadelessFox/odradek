package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.util.Arrays;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;
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

    private static final float ZOOM_STEP = 0.2f;
    private static final float ZOOM_MIN_LEVEL = (float) Math.pow(2, -5);
    private static final float ZOOM_MAX_LEVEL = (float) Math.pow(2, 7);

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
        var converted = texture.convert(TextureFormat.B8G8R8A8_UNORM);
        var surfaces = converted.surfaces().stream()
            .map(TextureViewer::createImage)
            .limit(texture.duration().isPresent() ? Long.MAX_VALUE : 1) // NOTE: dirty!
            .toList();

        var imageView = new ImageView();
        imageView.setImage(surfaces.getFirst());
        imageView.setZoomToFit(true);

        var imagePane = createImagePane(imageView);
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

    private static JComponent createImagePane(ImageView view) {
        var scrollPane = new JScrollPane();
        scrollPane.setViewport(new ImageViewport(view));
        scrollPane.addMouseWheelListener(e -> {
            float step = ZOOM_STEP * (float) -e.getPreciseWheelRotation();
            float oldZoom = view.getZoom();
            float newZoom = Math.clamp((float) Math.exp(Math.log(oldZoom) + step), ZOOM_MIN_LEVEL, ZOOM_MAX_LEVEL);

            if (oldZoom == newZoom) {
                return;
            }

            var viewport = scrollPane.getViewport();
            var point = SwingUtilities.convertPoint(viewport, e.getX(), e.getY(), view);
            var rect = viewport.getViewRect();
            rect.x = (int) Math.round(point.getX() * newZoom / oldZoom - point.getX() + rect.getX());
            rect.y = (int) Math.round(point.getY() * newZoom / oldZoom - point.getY() + rect.getY());

            // Zoom is now controlled by the user
            view.setZoomToFit(false);

            if (newZoom > oldZoom) {
                view.setZoom(newZoom);
                view.scrollRectToVisible(rect);
            } else {
                view.scrollRectToVisible(rect);
                view.setZoom(newZoom);
            }
        });

        return scrollPane;
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

    private static BufferedImage createImage(Surface surface) {
        var image = new BufferedImage(surface.width(), surface.height(), BufferedImage.TYPE_INT_ARGB);
        var imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0, len = surface.data().length; i < len; i += 4, o++) {
            imageData[o] = Arrays.getInt(surface.data(), i, ByteOrder.LITTLE_ENDIAN);
        }

        return image;
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

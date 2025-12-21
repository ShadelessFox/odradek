package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.util.Arrays;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;

public class TextureViewer implements Viewer<Texture> {
    private static final float ZOOM_STEP = 0.2f;
    private static final float ZOOM_MIN_LEVEL = (float) Math.pow(2, -5);
    private static final float ZOOM_MAX_LEVEL = (float) Math.pow(2, 7);

    @Override
    public JComponent createComponent(Texture texture) {
        var converted = texture.convert(TextureFormat.B8G8R8A8_UNORM);
        var surfaces = converted.surfaces().stream()
            .map(TextureViewer::createImage)
            .limit(texture.duration().isPresent() ? Long.MAX_VALUE : 1) // NOTE: dirty!
            .toList();

        var imageView = new ImageView();
        imageView.setImage(surfaces.getFirst());
        imageView.setZoomToFit(true);

        var imagePane = createImagePane(imageView);
        var imageToolbar = createToolBar(imageView);

        // To visually separate it from the toolbar
        imagePane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(imagePane, BorderLayout.CENTER);
        panel.add(imageToolbar, BorderLayout.NORTH);

        texture.duration().ifPresent(duration -> {
            var slider = new JSlider(0, surfaces.size() - 1);
            var animate = new JCheckBox("Animate", true);

            imageToolbar.addSeparator();
            imageToolbar.add(animate);
            imageToolbar.add(slider);
            imageToolbar.add(Box.createHorizontalBox());

            var delay = Math.toIntExact(duration.toMillis());
            var listener = new ActionListener() {
                private int index = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    imageView.setImage(surfaces.get(index));
                    slider.setValue(index);
                    index = (index + 1) % surfaces.size();
                }
            };
            var timer = new Timer(delay, listener);

            imageView.addHierarchyListener(e -> {
                if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED && SwingUtilities.isDescendingFrom(imageView, e.getChanged())) {
                    if (!imageView.isShowing()) {
                        timer.stop();
                    } else if (animate.isSelected() && !timer.isRunning()) {
                        timer.start();
                    }
                }
            });

            slider.addChangeListener(_ -> {
                int index = slider.getValue();
                listener.index = index;
                imageView.setImage(surfaces.get(index));
            });
            animate.addActionListener(_ -> {
                if (animate.isSelected()) {
                    timer.start();
                } else {
                    timer.stop();
                }
            });
        });

        return panel;
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

    private static JToolBar createToolBar(ImageView view) {
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

        var toolBar = new JToolBar();
        for (Channel channel : Channel.values()) {
            var button = new ToggleChannelButton(channel);
            button.setSelected(true);
            button.addActionListener(toggleChannel);
            button.addMouseListener(selectChannel);

            toolBar.add(button);
            buttons.add(button);
        }

        return toolBar;
    }

    private static BufferedImage createImage(Surface surface) {
        var image = new BufferedImage(surface.width(), surface.height(), BufferedImage.TYPE_INT_ARGB);
        var imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0, len = surface.data().length; i < len; i += 4, o++) {
            imageData[o] = Arrays.getInt(surface.data(), i, ByteOrder.LITTLE_ENDIAN);
        }

        return image;
    }

    @Override
    public String name() {
        return "Texture";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
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

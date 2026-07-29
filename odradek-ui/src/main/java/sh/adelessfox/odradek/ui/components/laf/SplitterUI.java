package sh.adelessfox.odradek.ui.components.laf;

import sh.adelessfox.odradek.ui.components.Orientation;
import sh.adelessfox.odradek.ui.components.tools.Splitter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class SplitterUI extends ComponentUI {
    private Splitter splitter;
    private Divider divider;

    @SuppressWarnings("unused")
    public static SplitterUI createUI(JComponent c) {
        return new SplitterUI();
    }

    @Override
    public void installUI(JComponent c) {
        splitter = (Splitter) c;

        divider = new Divider();
        divider.setOrientation(splitter.getOrientation());

        splitter.setLayout(new SplitterLayoutManager());
        splitter.add(divider, Splitter.DIVIDER);
        splitter.addPropertyChangeListener(e -> {
            switch (e.getPropertyName()) {
                case Splitter.ORIENTATION_PROPERTY -> {
                    divider.setOrientation(splitter.getOrientation());
                    splitter.revalidate();
                    splitter.repaint();
                }
                case Splitter.DIVIDER_LOCATION_PROPERTY -> {
                    splitter.revalidate();
                    splitter.repaint();
                }
            }
        });
    }

    @Override
    public void uninstallUI(JComponent c) {
        splitter.setLayout(null);
        splitter = null;
        divider = null;
    }

    private class Divider extends JPanel {
        Divider() {
            var handler = new Handler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
        }

        public void setOrientation(Orientation orientation) {
            if (orientation == Orientation.HORIZONTAL) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(6, 6);
        }

        private class Handler extends MouseAdapter {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                origin = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                origin = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin == null) {
                    return;
                }

                double weight;
                if (splitter.getOrientation() == Orientation.HORIZONTAL) {
                    var delta = e.getPoint().x - origin.x;
                    var location = getX() + delta;
                    weight = (double) location / (splitter.getWidth() - getPreferredSize().width);
                } else {
                    var delta = e.getPoint().y - origin.y;
                    var location = getY() + delta;
                    weight = (double) location / (splitter.getHeight() - getPreferredSize().height);
                }

                splitter.setDividerLocation(Math.clamp(weight, 0.0, 1.0));
            }
        }
    }

    private class SplitterLayoutManager implements LayoutManager {
        private int lastSplitterSize;

        @Override
        public void addLayoutComponent(String name, Component comp) {
            // don't care
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            // don't care
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(10, 10);
        }

        @Override
        public void layoutContainer(Container parent) {
            var splitter = (Splitter) parent;

            var firstComponent = splitter.getFirstComponent();
            var secondComponent = splitter.getSecondComponent();

            if (!contributes(firstComponent) && !contributes(secondComponent)) {
                // nothing to layout; just hide splitter
                divider.setBounds(0, 0, 0, 0);
            } else if (!contributes(firstComponent) || !contributes(secondComponent)) {
                // only one component; layout it to fill the whole area
                var child = contributes(firstComponent) ? firstComponent : secondComponent;
                child.setBounds(0, 0, parent.getWidth(), parent.getHeight());
                divider.setBounds(0, 0, 0, 0);
            } else {
                // both components are present; layout them with a splitter in between
                int totalWidth = parent.getWidth();
                int totalHeight = parent.getHeight();

                if (splitter.getOrientation() == Orientation.HORIZONTAL) {
                    if (lastSplitterSize == 0) {
                        lastSplitterSize = totalWidth;
                    } else if (lastSplitterSize != totalWidth) {
                        int delta = lastSplitterSize - totalWidth;
                        var oldLocation = lastSplitterSize * splitter.getDividerLocation();
                        var newLocation = (oldLocation - delta * splitter.getResizeWeight()) / totalWidth;
                        splitter.setDividerLocation(Math.clamp(newLocation, 0.0, 1.0));
                        lastSplitterSize = totalWidth;
                    }

                    int dividerWidth = divider.getPreferredSize().width;
                    int firstWidth = (int) (splitter.getDividerLocation() * (totalWidth - dividerWidth));
                    int secondWidth = totalWidth - dividerWidth - firstWidth;

                    firstComponent.setBounds(0, 0, firstWidth, totalHeight);
                    secondComponent.setBounds(firstWidth + dividerWidth, 0, secondWidth, totalHeight);
                    divider.setBounds(firstWidth, 0, dividerWidth, totalHeight);
                } else {
                    if (lastSplitterSize == 0) {
                        lastSplitterSize = totalHeight;
                    } else if (totalHeight != lastSplitterSize) {
                        int delta = lastSplitterSize - totalHeight;
                        var oldLocation = lastSplitterSize * splitter.getDividerLocation();
                        var newLocation = (oldLocation - delta * splitter.getResizeWeight()) / totalHeight;
                        splitter.setDividerLocation(Math.clamp(newLocation, 0.0, 1.0));
                        lastSplitterSize = totalHeight;
                    }

                    int dividerHeight = divider.getPreferredSize().height;
                    int firstHeight = (int) (splitter.getDividerLocation() * (totalHeight - dividerHeight));
                    int secondHeight = totalHeight - dividerHeight - firstHeight;

                    firstComponent.setBounds(0, 0, totalWidth, firstHeight);
                    secondComponent.setBounds(0, firstHeight + dividerHeight, totalWidth, secondHeight);
                    divider.setBounds(0, firstHeight, totalWidth, dividerHeight);
                }
            }
        }

        private boolean contributes(Component comp) {
            return comp != null && comp.isVisible();
        }
    }
}

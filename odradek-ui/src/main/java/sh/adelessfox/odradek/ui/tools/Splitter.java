package sh.adelessfox.odradek.ui.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class Splitter extends JComponent {
    enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    static final String ORIENTATION_PROPERTY = "orientation";
    static final String DIVIDER_LOCATION_PROPERTY = "dividerLocation";
    static final String RESIZE_WEIGHT_PROPERTY = "resizeWeight";

    static final String FIRST = "first";
    static final String SECOND = "second";
    static final String DIVIDER = "divider";

    private final Divider divider;
    private int dividerSize;

    private JComponent firstComponent;
    private JComponent secondComponent;
    private Orientation orientation = Orientation.HORIZONTAL;

    private double dividerLocation = 0.5;
    private double resizeWeight = 0.5;

    Splitter(Orientation orientation) {
        setOrientation(orientation);
        setLayout(new SplitterLayoutManager());

        divider = new Divider(this);
        divider.setOrientation(orientation);

        add(divider, Splitter.DIVIDER);
        addPropertyChangeListener(e -> {
            switch (e.getPropertyName()) {
                case Splitter.ORIENTATION_PROPERTY -> {
                    divider.setOrientation(getOrientation());
                    revalidate();
                    repaint();
                }
                case Splitter.DIVIDER_LOCATION_PROPERTY -> {
                    revalidate();
                    repaint();
                }
            }
        });

        updateUI();
    }

    public JComponent getFirstComponent() {
        return firstComponent;
    }

    public void setFirstComponent(JComponent c) {
        if (c != null) {
            add(c, Splitter.FIRST);
        } else if (firstComponent != null) {
            remove(firstComponent);
            firstComponent = null;
        }
    }

    public JComponent getSecondComponent() {
        return secondComponent;
    }

    public void setSecondComponent(JComponent c) {
        if (c != null) {
            add(c, Splitter.SECOND);
        } else if (secondComponent != null) {
            remove(secondComponent);
            secondComponent = null;
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        var oldOrientation = this.orientation;
        this.orientation = orientation;
        firePropertyChange(ORIENTATION_PROPERTY, oldOrientation, orientation);
    }

    public double getDividerLocation() {
        return dividerLocation;
    }

    public void setDividerLocation(double dividerLocation) {
        if (dividerLocation < 0.0 || dividerLocation > 1.0) {
            throw new IllegalArgumentException("Divider location must be between 0.0 and 1.0");
        }
        double oldLocation = this.dividerLocation;
        this.dividerLocation = dividerLocation;
        firePropertyChange(DIVIDER_LOCATION_PROPERTY, oldLocation, dividerLocation);
    }

    public double getResizeWeight() {
        return resizeWeight;
    }

    public void setResizeWeight(double resizeWeight) {
        if (resizeWeight < 0.0 || resizeWeight > 1.0) {
            throw new IllegalArgumentException("Resize weight must be between 0.0 and 1.0");
        }
        double oldWeight = this.resizeWeight;
        this.resizeWeight = resizeWeight;
        firePropertyChange(RESIZE_WEIGHT_PROPERTY, oldWeight, resizeWeight);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        dividerSize = UIManager.getInt("SplitPane.dividerSize");
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (!(constraints instanceof String str)) {
            throw new IllegalArgumentException("Constraints must be a String");
        }

        switch (str) {
            case FIRST -> {
                if (firstComponent != null) {
                    remove(firstComponent);
                }
                firstComponent = (JComponent) comp;
            }
            case SECOND -> {
                if (secondComponent != null) {
                    remove(secondComponent);
                }
                secondComponent = (JComponent) comp;
            }
            case DIVIDER -> {
                // Do nothing for divider, as it is managed by the UI
            }
            default ->
                throw new IllegalArgumentException("Constraints must be either Splitter.FIRST or Splitter.SECOND");
        }

        super.addImpl(comp, constraints, -1);

        revalidate();
        repaint();
    }

    @Override
    public void remove(Component component) {
        if (component == firstComponent) {
            firstComponent = null;
        } else if (component == secondComponent) {
            secondComponent = null;
        }
        super.remove(component);

        revalidate();
        repaint();
    }

    private static class Divider extends JPanel {
        private final Splitter splitter;

        Divider(Splitter splitter) {
            this.splitter = splitter;

            var handler = new Divider.Handler();
            addMouseListener(handler);
            addMouseMotionListener(handler);
        }

        void setOrientation(Orientation orientation) {
            if (orientation == Orientation.HORIZONTAL) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            }
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

    private static class SplitterLayoutManager implements LayoutManager {
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
            var divider = splitter.divider;
            var dividerSize = splitter.dividerSize;

            var firstComponent = splitter.getFirstComponent();
            var secondComponent = splitter.getSecondComponent();

            if (firstComponent == null && secondComponent == null) {
                // nothing to layout; just hide splitter
                divider.setBounds(0, 0, 0, 0);
            } else if (firstComponent == null || secondComponent == null) {
                // only one component; layout it to fill the whole area
                var child = firstComponent != null ? firstComponent : secondComponent;
                child.setBounds(0, 0, parent.getWidth(), parent.getHeight());
                divider.setBounds(0, 0, 0, 0);
            } else {
                // both components are present; layout them with a splitter in between
                int totalWidth = parent.getWidth();
                int totalHeight = parent.getHeight();

                if (splitter.getOrientation() == Orientation.HORIZONTAL) {
                    updateLocation(totalWidth, splitter);

                    int firstWidth = computeFirstComponentSize(totalWidth, splitter);
                    int secondWidth = computeSecondComponentSize(totalWidth, firstWidth, splitter);

                    firstComponent.setBounds(0, 0, firstWidth, totalHeight);
                    secondComponent.setBounds(firstWidth + dividerSize, 0, secondWidth, totalHeight);
                    divider.setBounds(firstWidth, 0, dividerSize, totalHeight);
                } else {
                    updateLocation(totalHeight, splitter);

                    int firstHeight = computeFirstComponentSize(totalHeight, splitter);
                    int secondHeight = computeSecondComponentSize(totalHeight, firstHeight, splitter);

                    firstComponent.setBounds(0, 0, totalWidth, firstHeight);
                    secondComponent.setBounds(0, firstHeight + dividerSize, totalWidth, secondHeight);
                    divider.setBounds(0, firstHeight, totalWidth, dividerSize);
                }
            }
        }

        private int computeFirstComponentSize(int totalSize, Splitter splitter) {
            return (int) (splitter.getDividerLocation() * (totalSize - splitter.dividerSize));
        }

        private int computeSecondComponentSize(int totalSize, int firstSize, Splitter splitter) {
            return totalSize - firstSize - splitter.dividerSize;
        }

        private void updateLocation(int totalSize, Splitter splitter) {
            if (lastSplitterSize == 0) {
                lastSplitterSize = totalSize;
            } else if (totalSize != lastSplitterSize) {
                int delta = lastSplitterSize - totalSize;
                var oldLocation = lastSplitterSize * splitter.getDividerLocation();
                var newLocation = (oldLocation - delta * splitter.getResizeWeight()) / totalSize;
                splitter.setDividerLocation(Math.clamp(newLocation, 0.0, 1.0));
                lastSplitterSize = totalSize;
            }
        }
    }
}

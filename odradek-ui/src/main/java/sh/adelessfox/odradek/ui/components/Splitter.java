package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.awt.*;

public final class Splitter extends JComponent {
    public static final String ORIENTATION_PROPERTY = "orientation";
    public static final String DIVIDER_LOCATION_PROPERTY = "dividerLocation";
    public static final String RESIZE_WEIGHT_PROPERTY = "resizeWeight";

    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String DIVIDER = "divider";

    private JComponent firstComponent;
    private JComponent secondComponent;
    private Orientation orientation = Orientation.HORIZONTAL;
    private double dividerLocation = 0.5;
    private double resizeWeight = 0.5;

    public Splitter(Orientation orientation) {
        setOrientation(orientation);
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

    @Override
    public String getUIClassID() {
        return "SplitterUI";
    }

    @Override
    public void updateUI() {
        setUI(UIManager.getUI(this));
    }

    @Override
    public SplitterUI getUI() {
        return (SplitterUI) super.getUI();
    }
}

package sh.adelessfox.odradek.ui.components.tools;

import sh.adelessfox.odradek.ui.components.Orientation;
import sh.adelessfox.odradek.ui.components.Splitter;

import javax.swing.*;
import java.awt.*;

/**
 * Container managing splitters for tool panels.
 */
final class SplitterContainer extends JPanel {
    private final Splitter leftSplitter;   // left / center
    private final Splitter rightSplitter;  // [leftSplitter] / right
    private final Splitter bottomSplitter; // [rightSplitter] / bottom

    private final Splitter leftInnerSplitter;
    private final Splitter rightInnerSplitter;
    private final Splitter bottomInnerSplitter;

    public SplitterContainer() {
        setLayout(new BorderLayout());

        leftInnerSplitter = new Splitter(Orientation.VERTICAL);
        leftInnerSplitter.setResizeWeight(0.5);

        rightInnerSplitter = new Splitter(Orientation.VERTICAL);
        rightInnerSplitter.setResizeWeight(0.5);

        bottomInnerSplitter = new Splitter(Orientation.HORIZONTAL);
        bottomInnerSplitter.setResizeWeight(0.5);

        leftSplitter = new Splitter(Orientation.HORIZONTAL);
        leftSplitter.setResizeWeight(0.0);

        rightSplitter = new Splitter(Orientation.HORIZONTAL);
        rightSplitter.setResizeWeight(1.0);

        bottomSplitter = new Splitter(Orientation.VERTICAL);
        bottomSplitter.setResizeWeight(1.0);

        rightSplitter.setFirstComponent(leftSplitter);
        bottomSplitter.setFirstComponent(rightSplitter);

        add(bottomSplitter, BorderLayout.CENTER);
    }

    void setCenter(JComponent center) {
        leftSplitter.setSecondComponent(center);
    }

    void setComponent(JComponent component, ToolPanel.Placement placement) {
        switch (placement.anchor()) {
            case LEFT -> {
                if (placement.primary()) {
                    leftInnerSplitter.setFirstComponent(component);
                } else {
                    leftInnerSplitter.setSecondComponent(component);
                }
                boolean hasLeft = leftInnerSplitter.getFirstComponent() != null || leftInnerSplitter.getSecondComponent() != null;
                leftSplitter.setFirstComponent(hasLeft ? leftInnerSplitter : null);
            }
            case RIGHT -> {
                if (placement.primary()) {
                    rightInnerSplitter.setFirstComponent(component);
                } else {
                    rightInnerSplitter.setSecondComponent(component);
                }
                boolean hasRight = rightInnerSplitter.getFirstComponent() != null || rightInnerSplitter.getSecondComponent() != null;
                rightSplitter.setSecondComponent(hasRight ? rightInnerSplitter : null);
            }
            case BOTTOM -> {
                if (placement.primary()) {
                    bottomInnerSplitter.setFirstComponent(component);
                } else {
                    bottomInnerSplitter.setSecondComponent(component);
                }
                boolean hasBottom = bottomInnerSplitter.getFirstComponent() != null || bottomInnerSplitter.getSecondComponent() != null;
                bottomSplitter.setSecondComponent(hasBottom ? bottomInnerSplitter : null);
            }
        }

        revalidate();
        repaint();
    }

    ToolState.Anchor.Weights getWeights(ToolPanel.Placement.Anchor anchor) {
        return switch (anchor) {
            case LEFT -> {
                double innerWeight = leftInnerSplitter.getDividerLocation();
                double outerWeight = leftSplitter.getDividerLocation();
                yield new ToolState.Anchor.Weights(innerWeight, outerWeight);
            }
            case RIGHT -> {
                double innerWeight = rightInnerSplitter.getDividerLocation();
                double outerWeight = rightSplitter.getDividerLocation();
                yield new ToolState.Anchor.Weights(innerWeight, outerWeight);
            }
            case BOTTOM -> {
                double innerWeight = bottomInnerSplitter.getDividerLocation();
                double outerWeight = bottomSplitter.getDividerLocation();
                yield new ToolState.Anchor.Weights(innerWeight, outerWeight);
            }
        };
    }

    void setWeights(ToolPanel.Placement.Anchor anchor, ToolState.Anchor.Weights weights) {
        switch (anchor) {
            case LEFT -> {
                leftInnerSplitter.setDividerLocation(weights.inner());
                leftSplitter.setDividerLocation(weights.outer());
            }
            case RIGHT -> {
                rightInnerSplitter.setDividerLocation(weights.inner());
                rightSplitter.setDividerLocation(weights.outer());
            }
            case BOTTOM -> {
                bottomInnerSplitter.setDividerLocation(weights.inner());
                bottomSplitter.setDividerLocation(weights.outer());
            }
        }
    }
}

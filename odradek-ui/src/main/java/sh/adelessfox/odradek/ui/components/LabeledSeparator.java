package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.util.Objects;

public class LabeledSeparator extends JSeparator {
    private String label;

    public LabeledSeparator(String label) {
        this(label, HORIZONTAL);
    }

    public LabeledSeparator(String label, int orientation) {
        super(orientation);
        setLabel(label);
    }

    @Override
    public String getUIClassID() {
        return "LabeledSeparatorUI";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        final String oldLabel = this.label;

        if (!Objects.equals(oldLabel, label)) {
            this.label = label;

            firePropertyChange("label", oldLabel, label);
            revalidate();
            repaint();
        }
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL) {
            throw new IllegalArgumentException("orientation must be HORIZONTAL");
        }

        super.setOrientation(orientation);
    }
}

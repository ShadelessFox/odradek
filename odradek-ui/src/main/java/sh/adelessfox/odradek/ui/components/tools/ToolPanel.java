package sh.adelessfox.odradek.ui.components.tools;

import javax.swing.*;

public interface ToolPanel {
    interface Provider {
        String id();

        String name();

        Icon icon();

        ToolPanel create();
    }

    record Placement(Anchor anchor, boolean primary) {
        public enum Anchor {
            LEFT,
            RIGHT,
            BOTTOM
        }
    }

    JComponent createComponent();
}

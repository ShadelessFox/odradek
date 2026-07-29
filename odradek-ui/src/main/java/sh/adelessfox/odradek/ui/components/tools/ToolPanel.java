package sh.adelessfox.odradek.ui.components.tools;

import javax.swing.*;

public interface ToolPanel {
    interface Provider {
        ToolPanel create(ToolSite site);

        String id();

        String name();

        Icon icon();
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

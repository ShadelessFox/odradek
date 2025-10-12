package sh.adelessfox.odradek.ui.components.toolwindow;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

final class ToolWindowGroup {
    private final Map<ToolWindowPane, ToolWindowInfo> panes = new HashMap<>();
    private final JPanel container;
    private final CardLayout layout;
    private ToolWindowPane selection;

    ToolWindowGroup() {
        layout = new CardLayout();
        container = new JPanel(layout);
    }

    void addPane(ToolWindowPane pane) {
        if (panes.containsKey(pane)) {
            throw new IllegalArgumentException("Pane is already added to this group");
        }
        panes.put(pane, new ToolWindowInfo(String.valueOf(panes.size())));
    }

    boolean hasPane(ToolWindowPane pane) {
        return panes.containsKey(pane);
    }

    boolean selectPane(ToolWindowPane pane) {
        if (selection == pane) {
            return false;
        }
        if (pane != null) {
            ToolWindowInfo info = panes.get(pane);
            if (info == null) {
                throw new IllegalArgumentException("Pane doesn't belong to this group");
            }
            if (info.component == null) {
                info.component = pane.createComponent();
                container.add(info.component, info.id);
            }
            layout.show(container, info.id);
        }
        selection = pane;
        return true;
    }

    boolean isSelected(ToolWindowPane pane) {
        return selection == pane;
    }

    boolean hasSelection() {
        return selection != null;
    }

    JComponent getComponent() {
        return container;
    }

    private static final class ToolWindowInfo {
        private final String id;
        private JComponent component;

        ToolWindowInfo(String id) {
            this.id = id;
        }
    }
}

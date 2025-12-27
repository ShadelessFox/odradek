package sh.adelessfox.odradek.ui.components.tool;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

final class ToolPanelGroup {
    private final Map<ToolPanel, ToolWindowInfo> panes = new HashMap<>();
    private final JPanel container;
    private final CardLayout layout;
    private ToolPanel selection;

    ToolPanelGroup() {
        layout = new CardLayout();
        container = new JPanel(layout);
    }

    void addPane(ToolPanel pane) {
        if (panes.containsKey(pane)) {
            throw new IllegalArgumentException("Pane is already added to this group");
        }
        panes.put(pane, new ToolWindowInfo(String.valueOf(panes.size())));
    }

    boolean hasPane(ToolPanel pane) {
        return panes.containsKey(pane);
    }

    boolean selectPane(ToolPanel pane) {
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

    boolean isSelected(ToolPanel pane) {
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

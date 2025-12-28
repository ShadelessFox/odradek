package sh.adelessfox.odradek.ui.components.tool;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

final class ToolPanelGroup {
    private final Map<ToolPanel, ToolWindowInfo> panels = new HashMap<>();
    private final JPanel container;
    private final CardLayout layout;
    private ToolPanel selection;

    ToolPanelGroup() {
        layout = new CardLayout();
        container = new JPanel(layout);
    }

    void addPanel(ToolPanel panel) {
        if (panels.containsKey(panel)) {
            throw new IllegalArgumentException("Panel is already added to this group");
        }
        panels.put(panel, new ToolWindowInfo(String.valueOf(panels.size())));
    }

    boolean hasPanel(ToolPanel pane) {
        return panels.containsKey(pane);
    }

    boolean selectPanel(ToolPanel panel) {
        if (selection == panel) {
            return false;
        }
        if (panel != null) {
            ToolWindowInfo info = panels.get(panel);
            if (info == null) {
                throw new IllegalArgumentException("Panel doesn't belong to this group");
            }
            if (info.component == null) {
                info.component = panel.createComponent();
                container.add(info.component, info.id);
            }
            layout.show(container, info.id);
        }
        selection = panel;
        return true;
    }

    boolean isSelected(ToolPanel panel) {
        return selection == panel;
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

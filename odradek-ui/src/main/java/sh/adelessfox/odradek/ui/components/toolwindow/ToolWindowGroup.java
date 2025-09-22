package sh.adelessfox.odradek.ui.components.toolwindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ToolWindowGroup {
    private final List<ToolWindowInfo> panes = new ArrayList<>();
    private final JPanel panel;
    private final CardLayout layout;
    private ToolWindowInfo selection;

    ToolWindowGroup() {
        layout = new CardLayout();
        panel = new JPanel(layout);
    }

    ToolWindowInfo addPane(ToolWindowPane pane) {
        if (findPane(pane) != null) {
            throw new IllegalArgumentException("Pane is already added to the group");
        }

        var component = pane.createComponent();
        var id = String.valueOf(panes.size());
        panel.add(component, id);

        var info = new ToolWindowInfo(id, pane);
        panes.add(info);

        return info;
    }

    ToolWindowInfo findPane(ToolWindowPane pane) {
        for (ToolWindowInfo info : panes) {
            if (info.pane() == pane) {
                return info;
            }
        }
        return null;
    }

    boolean selectPane(ToolWindowInfo info) {
        if (selection == info) {
            return false;
        }
        if (info != null) {
            layout.show(panel, info.id());
        }
        selection = info;
        return true;
    }

    boolean isSelected(ToolWindowInfo info) {
        return selection == info;
    }

    boolean hasSelection() {
        return selection != null;
    }

    JComponent getComponent() {
        return panel;
    }
}

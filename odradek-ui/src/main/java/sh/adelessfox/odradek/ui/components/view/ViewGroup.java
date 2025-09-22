package sh.adelessfox.odradek.ui.components.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ViewGroup {
    private final List<ViewInfo> views = new ArrayList<>();
    private final JPanel panel;
    private final CardLayout layout;
    private ViewInfo selection;

    ViewGroup() {
        layout = new CardLayout();
        panel = new JPanel(layout);
    }

    ViewInfo addView(View view) {
        if (findView(view) != null) {
            throw new IllegalArgumentException("View is already added to the group");
        }

        var component = view.createComponent();
        var id = String.valueOf(views.size());
        panel.add(component, id);

        var info = new ViewInfo(id, view);
        views.add(info);

        return info;
    }

    boolean selectView(ViewInfo info) {
        if (selection == info) {
            return false;
        }
        if (info != null) {
            layout.show(panel, info.id());
        }
        selection = info;
        return true;
    }

    boolean isSelected(ViewInfo info) {
        return selection == info;
    }

    boolean hasSelection() {
        return selection != null;
    }

    JComponent getComponent() {
        return panel;
    }

    private ViewInfo findView(View view) {
        for (ViewInfo info : views) {
            if (info.view() == view) {
                return info;
            }
        }
        return null;
    }
}

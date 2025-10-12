package sh.adelessfox.odradek.app.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.component.common.View;
import sh.adelessfox.odradek.app.component.graph.GraphView;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.toolwindow.ToolWindowPanel;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

@Singleton
public class MainView implements View<JComponent> {
    private final ToolWindowPanel root;
    private final FlatTabbedPane tabs;

    @Inject
    public MainView(GraphView graphView, EditorManager editorManager) {
        if (!(editorManager instanceof EditorStackManager editorStackManager)) {
            throw new IllegalStateException();
        }

        root = new ToolWindowPanel(ToolWindowPanel.Placement.LEFT);
        root.addPrimaryPane("Object graph", Fugue.getIcon("blue-document"), graphView);
        root.setContent(editorStackManager.getRoot());
        root.showPane(graphView);

        Actions.installContextMenu(tabs, ActionIds.TABS_MENU_ID, key -> {
            if (DataKeys.COMPONENT.is(key)) {
                return Optional.of(tabs);
            }
            return Optional.empty();
        });
    }

    @Override
    public JComponent getRoot() {
        return root;
    }
}

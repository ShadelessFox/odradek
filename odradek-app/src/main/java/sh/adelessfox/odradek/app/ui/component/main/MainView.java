package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.ui.components.toolwindow.ToolWindowPanel;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

@Singleton
public class MainView implements View<JComponent> {
    private final ToolWindowPanel root;

    @Inject
    public MainView(GraphPresenter graphPresenter, EditorManager editorManager) {
        if (!(editorManager instanceof EditorStackManager editorStackManager)) {
            throw new IllegalStateException();
        }

        root = new ToolWindowPanel(ToolWindowPanel.Placement.LEFT);
        root.addPrimaryPane("Object graph", Fugue.getIcon("blue-document"), graphPresenter.getView());
        root.setContent(editorStackManager.getRoot());
        root.showPane(graphPresenter.getView());
    }

    @Override
    public JComponent getRoot() {
        return root;
    }
}

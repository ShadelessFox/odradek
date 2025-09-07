package sh.adelessfox.odradek.app.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.component.common.View;
import sh.adelessfox.odradek.app.component.graph.GraphView;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;

import javax.swing.*;

@Singleton
public class MainView implements View<JComponent> {
    private final JSplitPane pane;

    @Inject
    public MainView(GraphView graphView, EditorManager editorManager) {
        if (!(editorManager instanceof EditorStackManager editorStackManager)) {
            throw new IllegalStateException();
        }

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(graphView.getRoot());
        pane.setRightComponent(editorStackManager.getRoot());
        pane.setDividerLocation(300);
    }

    @Override
    public JComponent getRoot() {
        return pane;
    }
}

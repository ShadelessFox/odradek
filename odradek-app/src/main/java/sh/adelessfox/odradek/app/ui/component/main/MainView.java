package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.bookmarks.BookmarkToolPanel;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.ui.components.tool.ToolPanelContainer;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

@Singleton
public class MainView implements View<JComponent> {
    private final ToolPanelContainer root;

    @Inject
    public MainView(
        GraphPresenter graphPresenter,
        BookmarkToolPanel bookmarkPanel,
        EditorManager editorManager
    ) {
        if (!(editorManager instanceof EditorStackManager editorStackManager)) {
            throw new IllegalStateException();
        }

        root = new ToolPanelContainer(ToolPanelContainer.Placement.LEFT);
        root.addPrimaryPane("Graph", Fugue.getIcon("blue-document"), graphPresenter.getView());
        root.addSecondaryPane("Bookmarks", Fugue.getIcon("blue-document-bookmark"), bookmarkPanel);
        root.setContent(editorStackManager.getRoot());
        root.showPane(graphPresenter.getView());
    }

    @Override
    public JComponent getRoot() {
        return root;
    }
}

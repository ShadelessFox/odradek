package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.bookmarks.BookmarkToolPanel;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.ui.components.tool.ToolPanelContainer;
import sh.adelessfox.odradek.ui.editors.EditorManager;
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
        root = new ToolPanelContainer(ToolPanelContainer.Placement.LEFT);
        root.addPrimaryPanel("Graph", Fugue.getIcon("blue-document"), graphPresenter.getView());
        root.addSecondaryPanel("Bookmarks", Fugue.getIcon("blue-document-bookmark"), bookmarkPanel);
        root.setContent(editorManager.getRoot());
        root.showPanel(graphPresenter.getView());
    }

    @Override
    public JComponent getRoot() {
        return root;
    }
}

package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.bookmarks.BookmarkToolPanel;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.app.ui.component.links.LinksToolPanel;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.components.tool.ToolPanelContainer;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

@Singleton
public class MainView implements View<JComponent> {
    public static final String GRAPH_PANEL_ID = "graph";
    public static final String BOOKMARKS_PANEL_ID = "bookmarks";
    public static final String LINKS_PANEL_ID = "links";

    private final ToolPanelContainer root;

    @Inject
    public MainView(
        GraphPresenter graphPresenter,
        BookmarkToolPanel bookmarkPanel,
        LinksToolPanel linksPanel,
        EditorManager editorManager,
        EventBus eventBus
    ) {
        root = new ToolPanelContainer(ToolPanelContainer.Placement.LEFT);
        root.addPrimaryPanel(GRAPH_PANEL_ID, "Graph", Fugue.getIcon("blue-document"), graphPresenter.getView());
        root.addSecondaryPanel(BOOKMARKS_PANEL_ID, "Bookmarks", Fugue.getIcon("blue-document-bookmark"), bookmarkPanel);
        root.addSecondaryPanel(LINKS_PANEL_ID, "Links", Fugue.getIcon("chain"), linksPanel);
        root.setContent(editorManager.getRoot());
        root.showPanel(GRAPH_PANEL_ID);

        eventBus.subscribe(MainEvent.ShowPanel.class, event -> root.showPanel(event.id()));
    }

    @Override
    public JComponent getRoot() {
        return root;
    }
}

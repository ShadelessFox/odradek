package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.bookmarks.BookmarkToolPanel;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.app.ui.component.usages.UsagesToolPanel;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.components.tool.ToolPanelContainer;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;

@Singleton
public class MainView implements View<JComponent> {
    public static final String GRAPH_PANEL_ID = "graph";
    public static final String BOOKMARKS_PANEL_ID = "bookmarks";
    public static final String USAGES_PANEL_ID = "usages";

    private final JPanel root;

    @Inject
    MainView(
        GraphPresenter graphPresenter,
        BookmarkToolPanel bookmarkPanel,
        UsagesToolPanel usagesPanel,
        EditorManager editorManager,
        EventBus eventBus
    ) {
        var center = buildCenter(graphPresenter, bookmarkPanel, usagesPanel, editorManager, eventBus);
        var bottom = buildBottom();

        root = new JPanel(new BorderLayout());
        root.add(center);
        root.add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public JComponent getRoot() {
        return root;
    }

    private static JComponent buildCenter(
        GraphPresenter graphPresenter,
        BookmarkToolPanel bookmarkPanel,
        UsagesToolPanel usagesPanel,
        EditorManager editorManager,
        EventBus eventBus
    ) {
        var center = new ToolPanelContainer(ToolPanelContainer.Placement.LEFT);
        center.addPrimaryPanel(GRAPH_PANEL_ID, "Graph", Fugue.getIcon("blue-document"), graphPresenter.getView());
        center.addSecondaryPanel(BOOKMARKS_PANEL_ID, "Bookmarks", Fugue.getIcon("blue-document-bookmark"), bookmarkPanel);
        center.addSecondaryPanel(USAGES_PANEL_ID, "Usages", Fugue.getIcon("magnifier-left"), usagesPanel);
        center.setContent(editorManager.getRoot());
        center.showPanel(GRAPH_PANEL_ID);

        eventBus.subscribe(MainEvent.ShowPanel.class, event -> center.showPanel(event.id()));
        return center;
    }

    private static JComponent buildBottom() {
        return new JToolBar();
    }
}

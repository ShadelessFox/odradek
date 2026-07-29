package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.bookmarks.BookmarkToolPanel;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.component.graph.GraphToolPanel;
import sh.adelessfox.odradek.app.ui.component.usages.UsagesToolPanel;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.components.tools.ToolContainer;
import sh.adelessfox.odradek.ui.components.tools.ToolPanel;
import sh.adelessfox.odradek.ui.editors.EditorManager;

import javax.swing.*;
import java.awt.*;

@Singleton
public class MainView implements View<JComponent> {
    private final JPanel root;

    @Inject
    MainView(
        GraphToolPanel.Provider graphPresenter,
        BookmarkToolPanel.Provider bookmarkPanel,
        UsagesToolPanel.Provider usagesPanel,
        EditorManager editorManager,
        EventBus eventBus
    ) {
        var center = buildCenter(graphPresenter, bookmarkPanel, usagesPanel, editorManager, eventBus);
        var right = buildRight();
        var bottom = buildBottom();

        root = new JPanel(new BorderLayout());
        root.add(center);
        root.add(right, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public JComponent getRoot() {
        return root;
    }

    private static JComponent buildCenter(
        GraphToolPanel.Provider graphPresenter,
        BookmarkToolPanel.Provider bookmarkPanel,
        UsagesToolPanel.Provider usagesPanel,
        EditorManager editorManager,
        EventBus eventBus
    ) {
        var center = new ToolContainer();
        center.addPanel(graphPresenter, new ToolPanel.Placement(ToolPanel.Placement.Anchor.LEFT, true));
        center.addPanel(bookmarkPanel, new ToolPanel.Placement(ToolPanel.Placement.Anchor.LEFT, false));
        center.addPanel(usagesPanel, new ToolPanel.Placement(ToolPanel.Placement.Anchor.LEFT, false));
        center.setCenter(editorManager.getRoot());
        center.openPanel(GraphToolPanel.ID, true);

        eventBus.subscribe(MainEvent.ShowPanel.class, event -> center.openPanel(event.id(), event.focus()));

        return center.getComponent();
    }

    private static Component buildRight() {
        return Box.createHorizontalStrut(5);
    }

    private static Component buildBottom() {
        return Box.createVerticalStrut(5);
    }
}

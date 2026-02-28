package sh.adelessfox.odradek.app.ui.component.links;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.game.LinkDatabase;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tool.ToolPanel;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StructuredTreeModel;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class LinksToolPanel implements ToolPanel {
    private static final Logger log = LoggerFactory.getLogger(LinksToolPanel.class);

    private final ForbiddenWestGame game;
    private final EventBus eventBus;

    private LinkDatabase database;
    private StructuredTree<LinkStructure> tree;

    @Inject
    public LinksToolPanel(ForbiddenWestGame game, EventBus eventBus) {
        this.game = game;
        this.eventBus = eventBus;

        try {
            database = LinkDatabase.open(game, Path.of(".export/links.db"));
        } catch (IOException e) {
            log.error("Failed to open link database", e);
        }

        eventBus.subscribe(LinkEvent.class, e -> {
            switch (e) {
                case LinkEvent.ShowFor(var objectId) -> show(objectId);
            }
        });
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>();
        tree.setLabelProvider(new LinkLabelProvider(game));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof ObjectIdHolder holder) {
                eventBus.publish(new GraphViewEvent.ShowObject(holder.objectId()));
            }
        }));

        Actions.installContextMenu(tree, GraphMenu.ID, key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            return tree.get(key);
        });

        return new JScrollPane(tree);
    }

    @Override
    public boolean isFocused() {
        return tree.isFocusOwner();
    }

    @Override
    public void setFocus() {
        tree.requestFocusInWindow();
    }

    private void show(ObjectId objectId) {
        tree.setModel(new StructuredTreeModel<>(new LinkStructure.Root(database, objectId)));
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
}

package sh.adelessfox.odradek.app.ui.component.links;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.tools.refs.LinkDatabase;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.ui.components.tool.ToolPanel;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public final class LinksToolPanel implements ToolPanel {
    private static final Logger log = LoggerFactory.getLogger(LinksToolPanel.class);

    private final ForbiddenWestGame game;
    private LinkDatabase database;
    private StructuredTree<LinkStructure> tree;

    @Inject
    public LinksToolPanel(ForbiddenWestGame game) {
        this.game = game;
        try {
            database = LinkDatabase.open(game, Path.of(".export/links.db"));
        } catch (IOException e) {
            log.error("Failed to open link database", e);
        }
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>(new LinkStructure.Root(database, new ObjectId(1624, 64)));
        tree.setLabelProvider(new LinkLabelProvider(game));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

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
}

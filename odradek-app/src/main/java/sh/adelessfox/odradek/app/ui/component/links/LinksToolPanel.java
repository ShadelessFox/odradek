package sh.adelessfox.odradek.app.ui.component.links;

import jakarta.inject.Inject;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.Event;
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
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public final class LinksToolPanel implements ToolPanel {
    private static final Logger log = LoggerFactory.getLogger(LinksToolPanel.class);

    private final ForbiddenWestGame game;
    private final EventBus appEventBus;

    private LinkDatabase database;

    @Inject
    public LinksToolPanel(ForbiddenWestGame game, EventBus appEventBus) {
        this.game = game;
        this.appEventBus = appEventBus;
    }

    @Override
    public JComponent createComponent() {
        var path = Path.of(".data/links.db");
        var eventBus = new DefaultEventBus();

        var layout = new CardLayout();
        var panel = new JPanel();
        panel.setLayout(layout);
        panel.add(createInitialView(eventBus), "init");
        panel.add(createLoadingView(), "loading");
        panel.add(createProgressView(eventBus), "scanning");
        panel.add(createTreeView(eventBus, game), "main");
        layout.show(panel, "init");

        eventBus.subscribe(Events.class, event -> {
            switch (event) {
                case Events.ScanStart _ -> {
                    layout.show(panel, "scanning");
                    new BuildDatabaseWorker(eventBus, game, path).execute();
                }
                case Events.DatabaseReady _ -> {
                    layout.show(panel, "loading");
                    new LoadDatabaseWorker(eventBus, game, path).execute();
                }
                case Events.DatabaseLoaded e -> {
                    database = e.database();
                    layout.show(panel, "main");
                }
                default -> { /* ignored */}
            }
        });

        // Forward show object request to the internal event bus
        appEventBus.subscribe(
            GraphViewEvent.ShowLinks.class,
            event -> eventBus.publish(new Events.ShowObject(event.objectId())));

        if (Files.exists(path)) {
            // Trigger loading immediately if database file already exists
            eventBus.publish(new Events.DatabaseReady());
        }

        return panel;
    }

    @Override
    public boolean isFocused() {
        // tree != null && tree.isFocusOwner();
        return false;
    }

    @Override
    public void setFocus() {
        // if (tree != null) {
        //     tree.requestFocusInWindow();
        // }
    }

    private JComponent createInitialView(EventBus eventBus) {
        JLabel text = new JLabel("Link database not found", SwingConstants.CENTER);

        JButton button = new JButton("Scan game resources");
        button.addActionListener(_ -> eventBus.publish(new Events.ScanStart()));

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("ins panel,fill", "al center", "[fill,grow][][][fill,grow]"));
        panel.add(text, "cell 0 1");
        panel.add(button, "cell 0 2");

        return panel;
    }

    private JComponent createLoadingView() {
        return new JLabel("Loading link database...", SwingConstants.CENTER);
    }

    private static JComponent createProgressView(EventBus eventBus) {
        var progress = new JProgressBar();
        var label = new JLabel("N/A");

        var panel = new JPanel();
        panel.setLayout(new MigLayout("ins panel,fill", "al center", "[fill,grow][][][][fill,grow]"));
        panel.add(new JLabel("Scanning game resources"), "cell 0 1");
        panel.add(progress, "cell 0 2,growx");
        panel.add(label, "cell 0 3");

        eventBus.subscribe(
            Events.ScanProgress.class,
            event -> {
                label.setText(event.progress().toString());
                progress.setMaximum(event.progress().max());
                progress.setValue(event.progress().cur());
            });

        return panel;
    }

    private JComponent createTreeView(EventBus eventBus, ForbiddenWestGame game) {
        var tree = new StructuredTree<LinkStructure>();
        tree.setLabelProvider(new LinkLabelProvider(game));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof ObjectIdHolder holder) {
                Application.getInstance().editors().openEditor(new ObjectEditorInputLazy(holder.objectId()));
            }
        }));

        Actions.installContextMenu(tree, GraphMenu.ID, key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            return tree.get(key);
        });

        eventBus.subscribe(Events.ShowObject.class, event -> {
            if (database == null) {
                return;
            }
            tree.setModel(new StructuredTreeModel<>(new LinkStructure.Root(database, event.objectId())));
            tree.expand();
        });

        return new JScrollPane(tree);
    }

    record Progress(int cur, int max) {
        @Override
        public String toString() {
            return "%d/%d".formatted(cur, max);
        }
    }

    private sealed interface Events extends Event {
        record ScanStart() implements Events {
        }

        record ScanProgress(Progress progress) implements Events {
        }

        record DatabaseReady() implements Events {
        }

        record DatabaseLoaded(LinkDatabase database) implements Events {
        }

        record ShowObject(ObjectId objectId) implements Events {
        }
    }

    private static class LoadDatabaseWorker extends SwingWorker<LinkDatabase, Void> {
        private final EventBus eventBus;
        private final ForbiddenWestGame game;
        private final Path path;

        LoadDatabaseWorker(EventBus eventBus, ForbiddenWestGame game, Path path) {
            this.eventBus = eventBus;
            this.game = game;
            this.path = path;
        }

        @Override
        protected LinkDatabase doInBackground() throws Exception {
            return LinkDatabase.open(game, path);
        }

        @Override
        protected void done() {
            try {
                eventBus.publish(new Events.DatabaseLoaded(get()));
            } catch (InterruptedException e) {
                log.debug("Link database load interrupted", e);
            } catch (ExecutionException e) {
                log.debug("Failed to load link database", e.getCause());
            }
        }
    }

    private static class BuildDatabaseWorker extends SwingWorker<Void, Progress> {
        private final EventBus eventBus;
        private final ForbiddenWestGame game;
        private final Path path;

        BuildDatabaseWorker(EventBus eventBus, ForbiddenWestGame game, Path path) {
            this.eventBus = eventBus;
            this.game = game;
            this.path = path;
        }

        @Override
        protected Void doInBackground() throws Exception {
            LinkDatabase.build(game, path, (cur, max) -> publish(new Progress(cur, max)));
            return null;
        }

        @Override
        protected void process(List<Progress> chunks) {
            eventBus.publish(new Events.ScanProgress(chunks.getLast()));
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (InterruptedException e) {
                log.debug("Link database build interrupted", e);
            } catch (ExecutionException e) {
                log.debug("Failed to build link database", e.getCause());
            }
            eventBus.publish(new Events.DatabaseReady());
        }
    }
}

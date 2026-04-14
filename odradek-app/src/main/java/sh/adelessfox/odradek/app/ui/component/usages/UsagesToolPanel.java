package sh.adelessfox.odradek.app.ui.component.usages;

import com.formdev.flatlaf.extras.components.FlatScrollPane;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
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
import sh.adelessfox.odradek.ui.util.Dialogs;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Singleton
public final class UsagesToolPanel implements ToolPanel {
    private static final Logger log = LoggerFactory.getLogger(UsagesToolPanel.class);

    private static final String CARD_SETUP = "setup";
    private static final String CARD_LOADING = "loading";
    private static final String CARD_SCANNING = "scanning";
    private static final String CARD_MAIN = "main";

    private final ForbiddenWestGame game;
    private final EventBus appEventBus;
    private final Path config;

    private final List<ObjectId> queue = new ArrayList<>();
    private int queueIndex;

    private LinkDatabase database;
    private ObjectId pendingObjectId;

    @Inject
    UsagesToolPanel(ForbiddenWestGame game, EventBus appEventBus, @Named("config") Path config) {
        this.game = game;
        this.appEventBus = appEventBus;
        this.config = config;
    }

    @Override
    public JComponent createComponent() {
        var path = determineDatabasePath(game, config);
        var eventBus = new DefaultEventBus();

        var layout = new CardLayout();
        var panel = new JPanel();
        panel.setLayout(layout);
        panel.add(createInitialView(eventBus), CARD_SETUP);
        panel.add(createLoadingView(), CARD_LOADING);
        panel.add(createProgressView(eventBus), CARD_SCANNING);
        panel.add(createTreeView(eventBus, game), CARD_MAIN);

        // Show the setup card by default
        layout.show(panel, CARD_SETUP);

        eventBus.subscribe(Events.class, event -> {
            switch (event) {
                case Events.ScanStart _ -> {
                    layout.show(panel, CARD_SCANNING);
                    new BuildDatabaseWorker(eventBus, game, path).execute();
                }
                case Events.DatabaseReady _ -> {
                    layout.show(panel, CARD_LOADING);
                    new LoadDatabaseWorker(eventBus, game, path).execute();
                }
                case Events.DatabaseLoaded e -> {
                    database = e.database();
                    layout.show(panel, CARD_MAIN);

                    if (pendingObjectId != null) {
                        eventBus.publish(new Events.ShowObject(pendingObjectId, false));
                        pendingObjectId = null;
                    }
                }
                case Events.DatabaseNotLoaded e -> {
                    Dialogs.showExceptionDialog(panel, "Unable to load the link database", e.reason());
                    layout.show(panel, CARD_SETUP);
                }
                default -> { /* ignored */}
            }
        });

        // Forward show object request to the internal event bus
        appEventBus.subscribe(
            MainEvent.ShowUsages.class,
            event -> eventBus.publish(new Events.ShowObject(event.objectId(), true)));

        if (Files.exists(path)) {
            // Trigger loading immediately if database file already exists
            eventBus.publish(new Events.DatabaseReady());
        }

        return panel;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setFocus() {
        // do nothing
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
        var tree = new StructuredTree<UsagesStructure>();
        tree.setLabelProvider(new UsagesLabelProvider(game));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setPlaceholderText("No object selected\n\nRight-click on an object to inspect its usages");
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
                pendingObjectId = event.objectId();
                return;
            }
            tree.setModel(new StructuredTreeModel<>(new UsagesStructure.Root(database, event.objectId())));
            tree.expand();
            if (event.record()) {
                addToQueue(event.objectId());
            }
        });

        var pane = new FlatScrollPane();
        pane.setViewportView(tree);
        pane.setStyle("border: 1,0,0,0, $Component.borderColor");

        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createToolBar(eventBus), BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);

        return panel;
    }

    private JToolBar createToolBar(EventBus eventBus) {
        var backAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (queueIndex > 1) {
                    queueIndex = Math.max(queueIndex - 1, 0);
                    eventBus.publish(new Events.ShowObject(queue.get(queueIndex - 1), false));
                }
            }
        };
        backAction.putValue(Action.SHORT_DESCRIPTION, "Back");
        backAction.putValue(Action.SMALL_ICON, Fugue.getIcon("arrow-180"));
        backAction.setEnabled(false);

        var forwardAction = new AbstractAction("Forward", Fugue.getIcon("arrow")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (queueIndex < queue.size()) {
                    int index = queueIndex;
                    queueIndex = Math.min(queueIndex + 1, queue.size());
                    eventBus.publish(new Events.ShowObject(queue.get(index), false));
                }
            }
        };
        forwardAction.putValue(Action.SHORT_DESCRIPTION, "Forward");
        forwardAction.putValue(Action.SMALL_ICON, Fugue.getIcon("arrow"));
        forwardAction.setEnabled(false);

        eventBus.subscribe(Events.ShowObject.class, _ -> {
            backAction.setEnabled(queueIndex > 1);
            forwardAction.setEnabled(queueIndex < queue.size());
        });

        var toolBar = new JToolBar();
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(new JLabel("Usages"));
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(backAction);
        toolBar.add(forwardAction);
        return toolBar;
    }

    private void addToQueue(ObjectId objectId) {
        queue.subList(queueIndex, queue.size()).clear();
        queue.add(objectId);
        queueIndex = queue.size();
    }

    private Path determineDatabasePath(ForbiddenWestGame game, Path config) {
        return config.resolve("links-" + LinkDatabase.computeHash(game) + ".db");
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

        record DatabaseNotLoaded(Throwable reason) implements Events {
        }

        record ShowObject(ObjectId objectId, boolean record) implements Events {
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
            log.debug("Loading link database from {}", path);
            return LinkDatabase.open(game, path);
        }

        @Override
        protected void done() {
            try {
                eventBus.publish(new Events.DatabaseLoaded(get()));
            } catch (InterruptedException e) {
                log.debug("Link database load interrupted", e);
                eventBus.publish(new Events.DatabaseNotLoaded(e));
            } catch (ExecutionException e) {
                log.debug("Failed to load link database", e.getCause());
                eventBus.publish(new Events.DatabaseNotLoaded(e.getCause()));
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
            log.debug("Building link database to {}", path);
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
                eventBus.publish(new Events.DatabaseReady());
            } catch (InterruptedException e) {
                log.debug("Link database build interrupted", e);
                eventBus.publish(new Events.DatabaseNotLoaded(e));
            } catch (ExecutionException e) {
                log.debug("Failed to build link database", e.getCause());
                eventBus.publish(new Events.DatabaseNotLoaded(e.getCause()));
            }
        }
    }
}

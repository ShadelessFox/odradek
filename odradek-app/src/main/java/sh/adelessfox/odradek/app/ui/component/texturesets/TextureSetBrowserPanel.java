package sh.adelessfox.odradek.app.ui.component.texturesets;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.component.main.MainView;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.decima.ContainerTypeRegistry;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.SearchTextField;
import sh.adelessfox.odradek.ui.components.tool.ToolPanel;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Singleton
public final class TextureSetBrowserPanel implements ToolPanel {
    private static final Logger log = LoggerFactory.getLogger(TextureSetBrowserPanel.class);
    private static final String TYPE_NAME = "TextureSet";

    private final DecimaGame game;
    private final EventBus eventBus;

    private JComponent root;
    private JList<Entry> list;
    private DefaultListModel<Entry> model;
    private volatile List<Entry> allEntries = List.of();
    private String filterText = "";

    @Inject
    TextureSetBrowserPanel(DecimaGame game, EventBus eventBus) {
        this.game = game;
        this.eventBus = eventBus;
    }

    @Override
    public JComponent createComponent() {
        if (root != null) {
            return root;
        }

        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setCellRenderer(new EntryRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    var entry = list.getSelectedValue();
                    if (entry != null) {
                        eventBus.publish(new MainEvent.ShowPanel(MainView.USAGES_PANEL_ID));
                        eventBus.publish(new MainEvent.ShowContainingModels(entry.objectId));
                    }
                }
            }
        });

        Actions.installContextMenu(list, GraphMenu.ID, key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            if (DataKeys.SELECTION.is(key)) {
                var selected = list.getSelectedValue();
                return selected == null ? Optional.empty() : Optional.of(selected);
            }
            return Optional.empty();
        });

        var filterField = new SearchTextField();
        filterField.setPlaceholderText("Filter by name…");
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterField.addActionListener(_ -> {
            filterText = filterField.getText().toLowerCase(Locale.ROOT);
            applyFilter();
        });

        var statusLabel = new JLabel("Scanning TextureSets…");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        var panel = new JPanel(new BorderLayout());
        panel.add(filterField, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        root = panel;

        new LoadEntriesWorker(statusLabel).execute();

        return root;
    }

    @Override
    public boolean isFocused() {
        return list != null && list.isFocusOwner();
    }

    @Override
    public void setFocus() {
        if (list != null) {
            list.requestFocusInWindow();
        }
    }

    private void applyFilter() {
        model.clear();
        if (filterText.isEmpty()) {
            allEntries.forEach(model::addElement);
            return;
        }
        for (Entry entry : allEntries) {
            var name = entry.name;
            if (name != null && name.toLowerCase(Locale.ROOT).contains(filterText)) {
                model.addElement(entry);
            }
        }
    }

    private static final class Entry implements ObjectIdHolder {
        final ObjectId objectId;
        volatile String name; // null until lazily resolved

        Entry(ObjectId objectId) {
            this.objectId = objectId;
        }

        @Override
        public ObjectId objectId() {
            return objectId;
        }
    }

    private static final class EntryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Entry e) {
                var name = e.name;
                if (name == null || name.isEmpty()) {
                    setText("[" + e.objectId + "]  <unnamed>");
                } else {
                    setText(name + "    [" + e.objectId + "]");
                }
            }
            return this;
        }
    }

    private final class LoadEntriesWorker extends SwingWorker<List<Entry>, Entry> {
        private final JLabel statusLabel;

        LoadEntriesWorker(JLabel statusLabel) {
            this.statusLabel = statusLabel;
        }

        @Override
        protected List<Entry> doInBackground() {
            var graph = game.streamingGraph();
            var registry = ContainerTypeRegistry.lookup(game).orElse(null);
            var entries = new ArrayList<Entry>();

            for (var group : graph.groups()) {
                var types = group.types();
                for (int i = 0; i < types.size(); i++) {
                    if (TYPE_NAME.equals(types.get(i).name())) {
                        entries.add(new Entry(new ObjectId(group.id(), i)));
                    }
                }
            }

            allEntries = List.copyOf(entries);
            SwingUtilities.invokeLater(TextureSetBrowserPanel.this::applyFilter);

            if (registry == null) {
                log.warn("No ContainerTypeRegistry available; TextureSet names will not be resolved");
                return entries;
            }

            int resolved = 0;
            for (Entry entry : entries) {
                try {
                    var object = game.readObject(entry.objectId);
                    registry.nameOf(object).ifPresent(name -> entry.name = name);
                } catch (Exception e) {
                    log.debug("Failed to read TextureSet {}", entry.objectId, e);
                }
                resolved++;
                if ((resolved & 0xff) == 0) {
                    int snapshot = resolved;
                    int total = entries.size();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Resolving names " + snapshot + "/" + total + "…");
                        list.repaint();
                    });
                }
            }

            return entries;
        }

        @Override
        protected void done() {
            try {
                var entries = get();
                statusLabel.setText(entries.size() + " TextureSets");
                list.repaint();
                applyFilter();
            } catch (InterruptedException e) {
                log.debug("TextureSet enumeration interrupted", e);
            } catch (ExecutionException e) {
                log.error("Failed to enumerate TextureSets", e.getCause());
                statusLabel.setText("Failed to load TextureSets");
            }
        }
    }

}

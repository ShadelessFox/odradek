package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.SearchTextField;
import sh.adelessfox.odradek.ui.tree.StructuredTree;
import sh.adelessfox.odradek.ui.tree.StructuredTreeModel;
import sh.adelessfox.odradek.ui.tree.TreeItem;
import sh.adelessfox.odradek.ui.util.ByteContents;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ApplicationWindow extends JComponent {
    private static final Logger log = LoggerFactory.getLogger(ApplicationWindow.class);

    private final FlatTabbedPane tabs;

    public ApplicationWindow(ForbiddenWestGame game) {
        tabs = new FlatTabbedPane();
        tabs.setTabPlacement(SwingConstants.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setTabsClosable(true);
        tabs.setTabCloseToolTipText("Close");
        tabs.setTabCloseCallback(JTabbedPane::remove);
        tabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isMiddleMouseButton(e)) {
                    return;
                }
                int index = tabs.indexAtLocation(e.getX(), e.getY());
                if (index >= 0) {
                    tabs.remove(index);
                }
            }
        });

        var tree = createGraphTree(game);
        var treeScrollPane = new JScrollPane(tree);

        var filterField = new SearchTextField();
        filterField.setPlaceholderText("Search by object type\u2026");
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        filterField.addActionListener(e -> {
            tree.getModel().setFilter(createFilter(e.getActionCommand()));
            tree.getModel().update();
            // TODO: Update the selection model as well
        });

        var treePanel = new JPanel(new BorderLayout());
        treePanel.add(filterField, BorderLayout.NORTH);
        treePanel.add(treeScrollPane, BorderLayout.CENTER);

        filterField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "focus-out");
        filterField.getActionMap().put("focus-out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.requestFocusInWindow();
            }
        });

        treeScrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F"), "focus-in");
        treeScrollPane.getActionMap().put("focus-in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterField.requestFocusInWindow();
            }
        });

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treePanel);
        splitPane.setRightComponent(tabs);
        splitPane.setDividerLocation(300);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    private Predicate<GraphStructure> createFilter(String filter) {
        if (filter.isBlank()) {
            return _ -> true;
        }
        if (filter.equals("has:roots")) {
            return structure -> switch (structure) {
                case GraphStructure.Group(var _, var group) -> group.rootCount() > 0;
                default -> true;
            };
        }
        return structure -> switch (structure) {
            case GraphStructure.Group(var graph, var group) -> graph.types(group).stream().anyMatch(info -> filterMatches(info, filter));
            case GraphStructure.GraphObjectSet(var _, var info, var _) -> filterMatches(info, filter);
            case GraphStructure.GroupObject object -> filterMatches(object.type(), filter);
            case GraphStructure.GroupObjectSet(var _, var _, var info, var _) -> filterMatches(info, filter);
            default -> true;
        };
    }

    private boolean filterMatches(TypeInfo info, String filter) {
        return info.toString().contains(filter);
    }

    private StructuredTree<GraphStructure> createGraphTree(ForbiddenWestGame game) {
        var model = new StructuredTreeModel<>(new GraphStructure.Graph(game.getStreamingGraph()));
        var tree = new StructuredTree<>(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLargeModel(true);
        tree.setCellRenderer(new GraphTreeCellRenderer());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof GraphStructure.GroupObject groupObject) {
                tree.setEnabled(false);
                var future = showObjectInfo(game, groupObject.group().groupID(), groupObject.index());
                future.whenComplete((_, _) -> tree.setEnabled(true));
            }
        });

        installGraphTreePopupMenu(tree);
        return tree;
    }

    private void installTreePopupMenu(StructuredTree<?> tree, BiConsumer<JPopupMenu, Object> callback) {
        var menu = new JPopupMenu();
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                var path = tree.getSelectionPath();
                if (path == null) {
                    return;
                }
                var component = path.getLastPathComponent();
                if (component instanceof TreeItem<?> item) {
                    component = item.getValue();
                }
                if (component != null) {
                    callback.accept(menu, component);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                menu.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                menu.removeAll();
            }
        });
        tree.setComponentPopupMenu(menu);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    var path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);
                        tree.scrollPathToVisible(path);
                    }
                }
            }
        });
    }

    private void installGraphTreePopupMenu(StructuredTree<?> tree) {
        installTreePopupMenu(tree, (menu, object) -> {
            if (!(object instanceof GraphStructure.GroupObjects objects)) {
                return;
            }

            var groupObjectsByType = new JCheckBoxMenuItem("Group objects by type");
            groupObjectsByType.setSelected(objects.options().contains(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE));
            groupObjectsByType.addActionListener(e -> {
                if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                    objects.options().add(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
                } else {
                    objects.options().remove(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
                }
                tree.getModel().update(tree.getSelectionPath());
            });
            menu.add(groupObjectsByType);

            if (groupObjectsByType.isSelected()) {
                var sortByCount = new JCheckBoxMenuItem("Sort by count");
                sortByCount.setSelected(objects.options().contains(GraphStructure.GroupObjects.Options.SORT_BY_COUNT));
                sortByCount.addActionListener(e -> {
                    if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                        objects.options().add(GraphStructure.GroupObjects.Options.SORT_BY_COUNT);
                    } else {
                        objects.options().remove(GraphStructure.GroupObjects.Options.SORT_BY_COUNT);
                    }
                    tree.getModel().update(tree.getSelectionPath());
                });
                menu.add(sortByCount);
            }
        });
    }

    private void installObjectTreePopupMenu(StructuredTree<?> tree) {
        installTreePopupMenu(tree, (menu, object) -> {
            if (!(object instanceof ObjectStructure structure)) {
                return;
            }
            if (structure.value() instanceof byte[] bytes) {
                menu.add(new AbstractAction("Copy to clipboard") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ByteContents contents = new ByteContents(bytes);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(contents, contents);
                    }
                });
            }
        });
    }

    private CompletableFuture<Void> showObjectInfo(ForbiddenWestGame game, int groupId, int objectIndex) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        new SwingWorker<StreamingObjectReader.GroupResult, Object>() {
            @Override
            protected StreamingObjectReader.GroupResult doInBackground() throws Exception {
                log.debug("Reading group {}", groupId);
                return game.getStreamingReader().readGroup(groupId);
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    var object = result.objects().get(objectIndex);
                    SwingUtilities.invokeLater(() -> showObjectInfo(game, object.type(), object.object(), groupId, objectIndex));
                } catch (ExecutionException e) {
                    log.error("Failed to read group {}", groupId, e.getCause());
                    future.completeExceptionally(e.getCause());
                } catch (InterruptedException e) {
                    log.error("Interrupted while reading group {}", groupId, e);
                    future.completeExceptionally(e);
                } finally {
                    future.complete(null);
                }
            }
        }.execute();

        return future;
    }

    private void showObjectInfo(Game game, ClassTypeInfo info, Object object, int groupId, int groupIndex) {
        log.debug("Showing object info for {} (group: {}, index: {})", info, groupId, groupIndex);

        FlatTabbedPane pane = new FlatTabbedPane();
        pane.setTabPlacement(SwingConstants.BOTTOM);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Converter.converters(object).forEach(converter -> {
            @SuppressWarnings("unchecked")
            var clazz = (Class<Object>) converter.resultType();
            Viewer.viewers(clazz).forEach(viewer -> {
                var result = converter.convert(object, game);
                if (result.isEmpty()) {
                    return;
                }
                pane.add(viewer.displayName(), viewer.createPreview(result.get()));
            });
        });

        pane.add("Object", new JScrollPane(createObjectTree(game, info, object)));
        pane.setSelectedIndex(0);

        tabs.add(info.toString(), pane);
        tabs.setToolTipTextAt(tabs.getTabCount() - 1, "Group: %d\nObject: %d".formatted(groupId, groupIndex));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        tabs.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F4"), "closeTab");
        tabs.getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabs.getSelectedIndex();
                if (index >= 0) {
                    tabs.removeTabAt(index);
                }
            }
        });
    }

    private StructuredTree<?> createObjectTree(Game game, ClassTypeInfo info, Object object) {
        var model = new StructuredTreeModel<>(new ObjectStructure.Compound(info, object));
        var tree = new StructuredTree<>(model);
        tree.setLargeModel(true);
        tree.setCellRenderer(new ObjectTreeCellRenderer());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                showObjectInfo(game, link.get().getType(), link.get(), link.group(), link.index());
            }
        });
        installObjectTreePopupMenu(tree);
        return tree;
    }
}

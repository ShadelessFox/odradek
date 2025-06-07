package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.extras.FlatSVGIcon;
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
import java.util.Objects;
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

        var filterField = createFilterField((input, matchCase, matchWholeWord) -> {
            tree.getModel().setFilter(createFilter(input, matchCase, matchWholeWord));
            tree.getModel().update();
            // TODO: Update the selection model as well
        });
        filterField.setPlaceholderText("Search by object type\u2026");
        filterField.setToolTipText("""
            <html>
            You can search for multiple type names separated by spaces.<br>
            - To include only groups that have <i>root objects</i>, use <b>has:roots</b>.<br>
            - To include only groups that have <i>child groups</i>, use <b>has:subgroups</b>.
            </html>
            """);
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

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

    private SearchTextField createFilterField(FilterListener listener) {
        var toggleCaseSensitive = new JToggleButton(Icons.CASE_SENSITIVE);
        toggleCaseSensitive.setToolTipText("Match Case");

        var toggleWholeWord = new JToggleButton(Icons.WHOLE_WORD);
        toggleWholeWord.setToolTipText("Match Whole Word");

        var filterToolbar = new JToolBar();
        filterToolbar.add(toggleCaseSensitive);
        filterToolbar.add(toggleWholeWord);
        filterToolbar.add(Box.createHorizontalStrut(4));

        var filterField = new SearchTextField();
        filterField.setTrailingComponent(filterToolbar);

        Runnable callback = () -> listener.filterChanged(
            filterField.getText(),
            toggleCaseSensitive.isSelected(),
            toggleWholeWord.isSelected()
        );

        filterField.addActionListener(_ -> callback.run());
        toggleCaseSensitive.addActionListener(_ -> callback.run());
        toggleWholeWord.addActionListener(_ -> callback.run());

        return filterField;
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

    private static Predicate<GraphStructure> createFilter(String input, boolean matchCase, boolean matchWholeWord) {
        if (input.isBlank()) {
            return null;
        }
        Predicate<GraphStructure> predicate = _ -> false;
        for (String part : input.split("\\s+")) {
            var filter = new Filter(part, matchCase, matchWholeWord);
            predicate = predicate.or(createFilterPart(part, filter));
        }
        return predicate;
    }

    private static Predicate<GraphStructure> createFilterPart(String input, Filter filter) {
        return switch (input) {
            case "has:subgroups" -> s -> !(s instanceof GraphStructure.Group(_, var group)) || group.subGroupCount() > 0;
            case "has:roots" -> s -> !(s instanceof GraphStructure.Group(_, var group)) || group.rootCount() > 0;
            default -> s -> switch (s) {
                case GraphStructure.Group(var graph, var group) -> graph.types(group).anyMatch(filter::matches);
                case GraphStructure.GraphObjectSet(_, var info, _) -> filter.matches(info);
                case GraphStructure.GroupObject object -> filter.matches(object.type());
                case GraphStructure.GroupObjectSet(_, _, var info, _) -> filter.matches(info);
                default -> true;
            };
        };
    }

    @FunctionalInterface
    private interface FilterListener {
        void filterChanged(String input, boolean matchCase, boolean matchWholeWord);
    }

    private record Filter(String query, boolean matchCase, boolean matchWholeWord) {
        boolean matches(TypeInfo info) {
            return matches(info.toString());
        }

        boolean matches(String input) {
            if (matchWholeWord && input.length() != query.length()) {
                return false;
            }
            if (matchCase) {
                if (matchWholeWord) {
                    return input.equals(query);
                } else {
                    return input.contains(query);
                }
            } else {
                int index = indexOfIgnoreCase(query, input);
                return index >= 0 && (!matchWholeWord || index == 0);
            }
        }

        private static int indexOfIgnoreCase(String key, String haystack) {
            if (haystack.length() < key.length()) {
                return -1;
            }
            for (int i = haystack.length() - key.length(); i >= 0; i--) {
                if (haystack.regionMatches(true, i, key, 0, key.length())) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static final class Icons {
        public static final FlatSVGIcon CASE_SENSITIVE = load("/icons/case-sensitive.svg");
        public static final FlatSVGIcon WHOLE_WORD = load("/icons/whole-word.svg");

        private Icons() {
        }

        private static FlatSVGIcon load(String path) {
            return new FlatSVGIcon(Objects.requireNonNull(Icons.class.getResource(path), path));
        }
    }
}

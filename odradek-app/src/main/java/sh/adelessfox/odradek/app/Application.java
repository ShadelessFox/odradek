package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.DocumentAdapter;
import sh.adelessfox.odradek.app.ui.tree.StructuredTree;
import sh.adelessfox.odradek.app.ui.tree.StructuredTreeModel;
import sh.adelessfox.odradek.app.ui.tree.TreeItem;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static FlatTabbedPane tabs;

    public static void main(String[] args) throws Exception {
        var source = Path.of("E:/SteamLibrary/steamapps/common/Horizon Forbidden West Complete Edition");
        var platform = EPlatform.WinGame;
        var game = new ForbiddenWestGame(source, platform);

        log.info("Opening UI");

        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        FlatLightLaf.setup();

        tabs = new FlatTabbedPane();
        tabs.setTabPlacement(SwingConstants.TOP);
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

        var filter = new FlatTextField();
        filter.setPlaceholderText("Filter objects\u2026");
        filter.setShowClearButton(true);
        filter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        var predicate = (Predicate<GraphStructure>) element -> switch (element) {
            case GraphStructure.Group(var graph, var group) -> IntStream.range(0, group.typeCount())
                .mapToObj(index -> graph.types().get(group.typeStart() + index))
                .anyMatch(type -> type.toString().contains(filter.getText()));
            case GraphStructure.GroupObject object -> object.type().toString().contains(filter.getText());
            case GraphStructure.GroupObjectSet(var _, var _, var info, var _) ->
                info.toString().contains(filter.getText());
            default -> true;
        };

        var structure = new FilteredStructure<>(new GraphStructure.Graph(game.getStreamingGraph()), predicate);
        var model = new StructuredTreeModel<>(structure);

        filter.getDocument().addDocumentListener(new DocumentAdapter() {
            private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            private ScheduledFuture<?> future;

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (future != null) {
                    future.cancel(false);
                }
                future = service.schedule(this::reload, 300, TimeUnit.MILLISECONDS);
            }

            private void reload() {
                SwingUtilities.invokeLater(model::reload);
            }
        });

        var tree = createGraphTree(game, model);
        var treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setBorder(BorderFactory.createEmptyBorder());

        var treePanel = new JPanel(new BorderLayout());
        treePanel.add(filter, BorderLayout.NORTH);
        treePanel.add(treeScrollPane, BorderLayout.CENTER);

        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treePanel);
        splitPane.setRightComponent(tabs);
        splitPane.setDividerLocation(300);

        var menuBar = new JMenuBar();
        menuBar.add(new JMenu("File"));
        menuBar.add(new JMenu("View"));
        menuBar.add(new JMenu("Help"));

        var frame = new JFrame();
        frame.add(splitPane);
        frame.setJMenuBar(menuBar);
        frame.setTitle("Odradek");
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static StructuredTree<?> createGraphTree(ForbiddenWestGame game, StructuredTreeModel<GraphStructure> model) {
        var tree = new StructuredTree<>(model);
        tree.setLargeModel(true);
        tree.setCellRenderer(new GraphTreeCellRenderer());
        tree.addActionListener(_ -> {
            var component = tree.getLastSelectedPathComponent();
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof GraphStructure.GroupObject groupObject) {
                tree.setEnabled(false);

                new SwingWorker<StreamingObjectReader.GroupResult, Object>() {
                    @Override
                    protected StreamingObjectReader.GroupResult doInBackground() throws Exception {
                        log.debug("Reading group {}", groupObject.group().groupID());
                        return game.getStreamingReader().readGroup(groupObject.group().groupID());
                    }

                    @Override
                    protected void done() {
                        try {
                            var result = get();
                            var object = result.objects().get(groupObject.index());
                            SwingUtilities.invokeLater(() -> showObjectInfo(object.type(), object.object()));
                        } catch (ExecutionException e) {
                            log.error("Failed to read object", e);
                        } catch (InterruptedException ignored) {
                            // ignored
                        } finally {
                            tree.setEnabled(true);
                        }
                    }
                }.execute();
            }
        });

        installGraphTreePopupMenu(tree);
        return tree;
    }

    private static void installGraphTreePopupMenu(StructuredTree<?> tree) {
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
                if (component instanceof GraphStructure.GroupObjects objects) {
                    var groupObjectsByType = new JCheckBoxMenuItem("Group objects by type");
                    groupObjectsByType.setSelected(objects.options().contains(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE));
                    groupObjectsByType.addActionListener(e -> {
                        if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                            objects.options().add(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
                        } else {
                            objects.options().remove(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
                        }
                        tree.getModel().reload(path);
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
                            tree.getModel().reload(path);
                        });
                        menu.add(sortByCount);
                    }
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

    private static void showObjectInfo(ClassTypeInfo info, Object object) {
        log.debug("Showing object info for {}", info);

        var tree = createObjectTree(info, object);

        var treePane = new JScrollPane(tree);
        treePane.setBorder(BorderFactory.createEmptyBorder());

        tabs.add(info.toString(), treePane);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private static StructuredTree<?> createObjectTree(ClassTypeInfo info, Object object) {
        var model = new StructuredTreeModel<>(new ObjectStructure.Compound(info, object));
        var tree = new StructuredTree<>(model);
        tree.setLargeModel(true);
        tree.setCellRenderer(new ObjectTreeCellRenderer());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure
                && structure.value() instanceof Ref<?> ref
                && ref.get() instanceof TypedObject target
            ) {
                showObjectInfo(target.getType(), target);
            }
        });
        return tree;
    }

}

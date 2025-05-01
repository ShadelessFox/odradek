package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.SearchTextField;
import sh.adelessfox.odradek.app.ui.tree.StructuredTree;
import sh.adelessfox.odradek.app.ui.tree.StructuredTreeModel;
import sh.adelessfox.odradek.app.ui.tree.TreeItem;
import sh.adelessfox.odradek.app.viewport.Camera;
import sh.adelessfox.odradek.app.viewport.Viewport;
import sh.adelessfox.odradek.app.viewport.renderpass.RenderMeshesPass;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.math.Vec3;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

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

    private static Predicate<GraphStructure> createFilter(String filter) {
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
            case GraphStructure.Group(var graph, var group) ->
                graph.types(group).stream().anyMatch(info -> filterMatches(info, filter));
            case GraphStructure.GraphObjectSet(var _, var info, var _) -> filterMatches(info, filter);
            case GraphStructure.GroupObject object -> filterMatches(object.type(), filter);
            case GraphStructure.GroupObjectSet(var _, var _, var info, var _) -> filterMatches(info, filter);
            default -> true;
        };
    }

    private static boolean filterMatches(TypeInfo info, String filter) {
        return info.toString().contains(filter);
    }

    private static StructuredTree<GraphStructure> createGraphTree(ForbiddenWestGame game) {
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
                            SwingUtilities.invokeLater(() -> showObjectInfo(game, object.type(), object.object()));
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
                        tree.getModel().update(path);
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
                            tree.getModel().update(path);
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

    private static void showObjectInfo(Game game, ClassTypeInfo info, Object object) {
        log.debug("Showing object info for {}", info);

        JComponent component;

        boolean matches = Converter.converters()
            .anyMatch(converter -> converter.supports(object) && converter.resultType() == Node.class);

        if (matches) {
            var scene = Converter.convert(object, game, Node.class)
                .map(Scene::of)
                .orElse(null);

            Viewport viewport = new Viewport();
            viewport.setMinimumSize(new Dimension(100, 100));
            viewport.addRenderPass(new RenderMeshesPass());
            viewport.setCamera(new Camera(30.f, 0.01f, 1000.f));
            viewport.setScene(scene);

            component = viewport;
        } else {
            component = new JScrollPane(createObjectTree(game, info, object));
        }

        tabs.add(info.toString(), component);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private static StructuredTree<?> createObjectTree(Game game, ClassTypeInfo info, Object object) {
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
                showObjectInfo(game, target.getType(), target);
            }
        });
        return tree;
    }

}

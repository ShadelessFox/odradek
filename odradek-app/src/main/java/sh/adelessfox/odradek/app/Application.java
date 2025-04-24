package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.tree.PaginatedTreeModel;
import sh.adelessfox.odradek.app.ui.tree.StructuredTreeModel;
import sh.adelessfox.odradek.app.ui.tree.Tree;
import sh.adelessfox.odradek.app.ui.tree.TreeItem;
import sh.adelessfox.odradek.app.ui.util.Fugue;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static FlatTabbedPane tabs;

    public static void main(String[] args) throws Exception {
        var source = Path.of("E:/SteamLibrary/steamapps/common/Horizon Forbidden West Complete Edition");
        var platform = EPlatform.WinGame;
        var game = new ForbiddenWestGame(source, platform);

        log.info("Opening UI");

        UIManager.getDefaults().addResourceBundle("sh.adelessfox.odradek.app.ui.util.Bundle");
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

        var tree = createGraphTree(game);
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

    private static Tree createGraphTree(ForbiddenWestGame game) {
        var model = new StructuredTreeModel<>(new GraphStructure(game.getStreamingGraph()));
        var tree = new Tree(model);
        tree.setLargeModel(true);
        tree.setCellRenderer(new GraphTreeCellRenderer());
        tree.addActionListener(_ -> {
            var component = tree.getLastSelectedPathComponent();
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof GraphStructure.Element.Compound element) {
                tree.setEnabled(false);

                new SwingWorker<StreamingObjectReader.GroupResult, Object>() {
                    @Override
                    protected StreamingObjectReader.GroupResult doInBackground() throws Exception {
                        log.debug("Reading group {}", element.group().groupID());
                        return game.getStreamingReader().readGroup(element.group().groupID());
                    }

                    @Override
                    protected void done() {
                        try {
                            var result = get();
                            var object = result.objects().get(element.index());
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

    private static void installGraphTreePopupMenu(Tree tree) {
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
                if (component instanceof GraphStructure.Element.GroupObjects objects) {
                    var groupObjectsByType = new JCheckBoxMenuItem("Group objects by type");
                    groupObjectsByType.setSelected(objects.options().contains(GraphStructure.Element.GroupObjects.Options.GROUP_BY_TYPE));
                    groupObjectsByType.addActionListener(e -> {
                        if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                            objects.options().add(GraphStructure.Element.GroupObjects.Options.GROUP_BY_TYPE);
                        } else {
                            objects.options().remove(GraphStructure.Element.GroupObjects.Options.GROUP_BY_TYPE);
                        }
                        tree.getModel().unload(path.getLastPathComponent());
                        tree.getModel().nodeStructureChanged(path);
                    });
                    menu.add(groupObjectsByType);

                    if (groupObjectsByType.isSelected()) {
                        var sortByCount = new JCheckBoxMenuItem("Sort by count");
                        sortByCount.setSelected(objects.options().contains(GraphStructure.Element.GroupObjects.Options.SORT_BY_COUNT));
                        sortByCount.addActionListener(e -> {
                            if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                                objects.options().add(GraphStructure.Element.GroupObjects.Options.SORT_BY_COUNT);
                            } else {
                                objects.options().remove(GraphStructure.Element.GroupObjects.Options.SORT_BY_COUNT);
                            }
                            tree.getModel().unload(path.getLastPathComponent());
                            tree.getModel().nodeStructureChanged(path);
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
        tree.setLargeModel(true);

        var treePane = new JScrollPane(tree);
        treePane.setBorder(BorderFactory.createEmptyBorder());

        tabs.add(info.toString(), treePane);
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private static Tree createObjectTree(ClassTypeInfo info, Object object) {
        var model = new PaginatedTreeModel(100, new StructuredTreeModel<>(new ObjectStructure(info, object)));
        var tree = new Tree(model);
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            var row = tree.getLeadSelectionRow();
            if (model.handleNextPage(component, event.getSource().isShiftDown())) {
                tree.setSelectionRow(row);
                return;
            }
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure.Element element
                && element.value() instanceof Ref<?> ref
                && ref.get() instanceof TypedObject target
            ) {
                showObjectInfo(target.getType(), target);
            }
        });
        return tree;
    }

    private static class GraphTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
        ) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Object node = value;
            if (value instanceof TreeItem<?> item) {
                node = item.getValue();
            }
            if (node instanceof GraphStructure.Element element) {
                var icon = switch (element) {
                    case GraphStructure.Element.Root ignored -> Fugue.getIcon("folders-stack");
                    case GraphStructure.Element.Group ignored -> Fugue.getIcon("folders");
                    case GraphStructure.Element.GroupDependentGroups ignored -> Fugue.getIcon("folder-import");
                    case GraphStructure.Element.GroupDependencyGroups ignored -> Fugue.getIcon("folder-export");
                    case GraphStructure.Element.GroupRoots ignored -> Fugue.getIcon("folder-bookmark");
                    case GraphStructure.Element.GroupObjects ignored -> Fugue.getIcon("folder-open-document");
                    case GraphStructure.Element.GroupObjectSet ignored -> Fugue.getIcon("folder-open-document");
                    case GraphStructure.Element.Compound ignored -> Fugue.getIcon("blue-document");
                };
                if (tree.isEnabled()) {
                    setIcon(icon);
                } else {
                    Icon disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(tree, icon);
                    setDisabledIcon(disabledIcon != null ? disabledIcon : icon);
                }
            }

            return this;
        }
    }
}

package sh.adelessfox.odradek.app.component.graph;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.component.common.View;
import sh.adelessfox.odradek.app.menu.graph.GraphMenu;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.SearchTextField;
import sh.adelessfox.odradek.ui.components.ValidationPopup;
import sh.adelessfox.odradek.ui.components.toolwindow.ToolWindowPane;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

@Singleton
public class GraphView implements View<JComponent>, ToolWindowPane {
    private final EventBus eventBus;
    private final ForbiddenWestGame game;

    private final StructuredTree<GraphStructure> tree;
    private final JPanel panel;
    private final ValidationPopup filterValidationPopup;

    @Inject
    public GraphView(EventBus eventBus, ForbiddenWestGame game) {
        this.eventBus = eventBus;
        this.game = game;

        var filterField = createFilterField();
        filterField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "focus-out");
        filterField.getActionMap().put("focus-out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.requestFocusInWindow();
            }
        });

        filterValidationPopup = new ValidationPopup(filterField);

        tree = createGraphTree();

        var treeScrollPane = new JScrollPane(tree);
        treeScrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F"), "focus-in");
        treeScrollPane.getActionMap().put("focus-in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterField.requestFocusInWindow();
            }
        });

        panel = new JPanel(new BorderLayout());
        panel.add(filterField, BorderLayout.NORTH);
        panel.add(treeScrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(300, 300));

        Actions.installContextMenu(tree, GraphMenu.ID, key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            return tree.get(key);
        });
    }

    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isFocused() {
        return tree.isFocusOwner();
    }

    @Override
    public void setFocus() {
        tree.requestFocusInWindow();
    }

    @Override
    public JComponent getRoot() {
        return panel;
    }

    public StructuredTree<GraphStructure> getTree() {
        return tree;
    }

    public ValidationPopup getFilterValidationPopup() {
        return filterValidationPopup;
    }

    private SearchTextField createFilterField() {
        var toggleCaseSensitive = new JToggleButton(Fugue.getIcon("edit-small-caps"));
        toggleCaseSensitive.setToolTipText("Match Case");

        var toggleWholeWord = new JToggleButton(Fugue.getIcon("edit-space"));
        toggleWholeWord.setToolTipText("Match Whole Word");

        var filterToolbar = new JToolBar();
        filterToolbar.add(toggleCaseSensitive);
        filterToolbar.add(toggleWholeWord);
        filterToolbar.add(Box.createHorizontalStrut(4));

        var filterField = new SearchTextField();
        filterField.setTrailingComponent(filterToolbar);
        filterField.setPlaceholderText("Search by object type\u2026");
        filterField.setToolTipText("""
            <html>
            You can search for multiple type names separated by spaces.<br>
            - To include groups that have <i>root objects</i>, use <b>has:roots</b>.<br>
            - To include groups that have <i>child groups</i>, use <b>has:subgroups</b>.<br>
            - To include a particular group, use <b>group:groupId</b>, where <i>groupId</i> is the id of a group.
            </html>
            """);
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        Runnable callback = () -> eventBus.publish(new GraphViewEvent.UpdateFilter(
            filterField.getText(),
            toggleCaseSensitive.isSelected(),
            toggleWholeWord.isSelected()
        ));

        filterField.addActionListener(_ -> callback.run());
        toggleCaseSensitive.addActionListener(_ -> callback.run());
        toggleWholeWord.addActionListener(_ -> callback.run());

        return filterField;
    }

    private StructuredTree<GraphStructure> createGraphTree() {
        var tree = new StructuredTree<>(new GraphStructure.Graph(game.getStreamingGraph()));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLabelProvider(new TreeLabelProvider<>() {
            @Override
            public Optional<String> getText(GraphStructure element) {
                return Optional.of(element.toString());
            }

            @Override
            public Optional<Icon> getIcon(GraphStructure element) {
                return Optional.ofNullable(switch (element) {
                    case GraphStructure.Graph _ -> null;
                    case GraphStructure.GraphGroups _, GraphStructure.GraphObjects _ -> Fugue.getIcon("folders-stack");
                    case GraphStructure.Group _ -> Fugue.getIcon("folders");
                    case GraphStructure.GroupDependencies _ -> Fugue.getIcon("folder-export");
                    case GraphStructure.GroupDependents _ -> Fugue.getIcon("folder-import");
                    case GraphStructure.GroupObject _ -> Fugue.getIcon("blue-document");
                    case GraphStructure.GraphRoots _,
                         GraphStructure.GroupRoots _ -> Fugue.getIcon("folder-bookmark");
                    case GraphStructure.GroupableByType _,
                         GraphStructure.GroupedByType _,
                         GraphStructure.GraphObjectSet _ -> Fugue.getIcon("folder-open-document");
                });
            }
        });
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof GraphStructure.GroupObject groupObject) {
                eventBus.publish(new GraphViewEvent.ShowObject(
                    groupObject.group().groupID(),
                    groupObject.index()
                ));
            }
        });
        return tree;
    }
}

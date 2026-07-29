package sh.adelessfox.odradek.app.ui.tools.graph;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.PreviewManager;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.tools.graph.filter.Filter;
import sh.adelessfox.odradek.app.ui.tools.graph.filter.FilterOption;
import sh.adelessfox.odradek.app.ui.tools.graph.filter.FilterResult;
import sh.adelessfox.odradek.app.ui.tools.graph.menu.GraphMenu;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.Focusable;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.SearchTextField;
import sh.adelessfox.odradek.ui.components.ValidationPopup;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;
import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.tools.ToolPanel;
import sh.adelessfox.odradek.ui.tools.ToolSite;
import sh.adelessfox.odradek.ui.util.Fugue;
import sh.adelessfox.odradek.util.Result;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

public class GraphToolPanel implements ToolPanel, Focusable {
    public static final String ID = "graph";

    @Singleton
    public static final class Provider implements ToolPanel.Provider {
        private final EventBus eventBus;
        private final DecimaGame game;

        @Inject
        Provider(EventBus eventBus, DecimaGame game) {
            this.eventBus = eventBus;
            this.game = game;
        }

        @Override
        public ToolPanel create(ToolSite site) {
            return new GraphToolPanel(site, eventBus, game);
        }

        @Override
        public String id() {
            return GraphToolPanel.ID;
        }

        @Override
        public String name() {
            return "Graph";
        }

        @Override
        public Icon icon() {
            return Fugue.getIcon("blue-document");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(GraphToolPanel.class);

    private final ToolSite site;
    private final EventBus eventBus;
    private final DecimaGame game;

    private StructuredTree<GraphStructure> tree;
    private SearchTextField filterField;

    private GraphToolPanel(ToolSite site, EventBus eventBus, DecimaGame game) {
        this.site = site;
        this.eventBus = eventBus;
        this.game = game;
    }

    @Override
    public JComponent createComponent() {
        tree = createGraphTree();
        tree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ctrl F"), "focus-in");
        tree.getActionMap().put("focus-in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterField.requestFocusInWindow();
            }
        });

        filterField = createFilterField(tree);
        filterField.setBorder(BorderFactory.createEmptyBorder());
        filterField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "focus-out");
        filterField.getActionMap().put("focus-out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tree.requestFocusInWindow();
            }
        });

        Actions.installContextMenu(tree, GraphMenu.ID, key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            return tree.get(key);
        });

        site.setLeadingComponent(filterField);

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

    private SearchTextField createFilterField(StructuredTree<GraphStructure> tree) {
        var toggleCaseSensitive = new JToggleButton(Fugue.getIcon("edit-small-caps"));
        toggleCaseSensitive.setToolTipText("Match Case");

        var toggleWholeWord = new JToggleButton(Fugue.getIcon("edit-space"));
        toggleWholeWord.setToolTipText("Match Whole Word");

        var filterToolbar = new JToolBar();
        filterToolbar.add(toggleCaseSensitive);
        filterToolbar.add(toggleWholeWord);

        var filterField = new SearchTextField();
        filterField.setShowClearButton(true);
        filterField.setTrailingComponent(filterToolbar);
        filterField.setPlaceholderText("Search by object type\u2026");
        filterField.setToolTipText("""
            <html>
            Search uses a simple query language:<br>
             - Use <code>not</code>, <code>and</code>, <code>or</code> (in order of priority) operators to combine conditions.<br>
             - Use parentheses <code>()</code> to group conditions.<br>
            You can also search based on specific attributes:<br>
             - To include only a matching type name, use <code>type:&lt;typeName&gt;</code>.<br>
             - To include only a particular group, use <code>group:&lt;groupId&gt;</code><br>
             - To include groups that have <i>root objects</i>, use <code>has:roots</code>.<br>
             - To include groups that have <i>child groups</i>, use <code>has:subgroups</code>.
            </html>
            """);

        var validationPopup = new ValidationPopup(filterField);
        Runnable callback = () -> {
            var filter = createFilter(
                filterField.getText(),
                toggleCaseSensitive.isSelected(),
                toggleWholeWord.isSelected());

            switch (filter) {
                case Result.Ok(var predicate) -> {
                    validationPopup.setVisible(false);
                    tree.getModel().setFilter(predicate.orElse(null));
                    tree.getModel().refresh();
                }
                case Result.Error(var message) -> {
                    validationPopup.setMessage(message);
                    validationPopup.setSeverity(ValidationPopup.Severity.ERROR);
                    validationPopup.setVisible(true);
                }
            }
        };

        filterField.addActionListener(_ -> callback.run());
        toggleCaseSensitive.addActionListener(_ -> callback.run());
        toggleWholeWord.addActionListener(_ -> callback.run());

        return filterField;
    }

    private StructuredTree<GraphStructure> createGraphTree() {
        var tree = new StructuredTree<>(new GraphStructure.Graph(game.streamingGraph()));
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
                         GraphStructure.GroupableByGroup _,
                         GraphStructure.GroupedByGroup _ -> Fugue.getIcon("folder-open-document");
                });
            }
        });
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof GraphStructure.GroupObject groupObject) {
                eventBus.publish(new MainEvent.ShowObject(new ObjectId(
                    groupObject.group().id(),
                    groupObject.index())
                ));
            }
        }));

        PreviewManager.install(tree, game, new PreviewManager.PreviewObjectProvider() {
            @Override
            public Optional<TypeInfo> getType(JTree tree, Object value) {
                if (!Application.getInstance().settings().showObjectPreview().orElse(false)) {
                    return Optional.empty();
                }
                if (value instanceof ObjectIdHolder provider) {
                    return Optional.of(provider.objectType(game));
                }
                return Optional.empty();
            }

            @Override
            public Optional<TypedObject> getObject(JTree tree, Object value) {
                var holder = (ObjectIdHolder) value;
                try {
                    return Optional.of(game.readObject(holder.objectId()));
                } catch (IOException e) {
                    log.error("Failed to read object for preview", e);
                }
                return Optional.empty();
            }
        });

        return tree;
    }

    private static Result<Optional<Predicate<GraphStructure>>, String> createFilter(
        String input,
        boolean matchCase,
        boolean matchWholeWord
    ) {
        if (input.isBlank()) {
            return Result.ok(Optional.empty());
        }
        var options = EnumSet.noneOf(FilterOption.class);
        if (matchCase) {
            options.add(FilterOption.CASE_SENSITIVE);
        }
        if (matchWholeWord) {
            options.add(FilterOption.WHOLE_WORD);
        }
        return Filter.parse(input)
            .map(filter -> Optional.<Predicate<GraphStructure>>of(
                structure -> filter.test(structure, options) != FilterResult.FAIL))
            .mapError(error -> "%s at %s".formatted(error.message(), error.location()));
    }
}

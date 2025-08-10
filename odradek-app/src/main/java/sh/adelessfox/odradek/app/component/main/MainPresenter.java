package sh.adelessfox.odradek.app.component.main;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.Futures;
import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.app.component.common.Presenter;
import sh.adelessfox.odradek.app.component.graph.GraphPresenter;
import sh.adelessfox.odradek.app.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class MainPresenter implements Presenter<MainView> {
    private static final String PROP_GROUP_ID = "odradek.groupId";
    private static final String PROP_OBJECT_INDEX = "odradek.objectIndex";

    private static final Logger log = LoggerFactory.getLogger(MainPresenter.class);

    private final ForbiddenWestGame game;
    private final MainView view;

    @Inject
    public MainPresenter(
        ForbiddenWestGame game,
        GraphPresenter graphPresenter,
        MainView view,
        EventBus eventBus
    ) {
        this.game = game;
        this.view = view;

        eventBus.subscribe(GraphViewEvent.ShowObject.class, event -> {
            if (revealObjectInfo(event.groupId(), event.objectIndex())) {
                return;
            }
            graphPresenter.setBusy(true);
            var future = showObjectInfo(event.groupId(), event.objectIndex());
            future.whenComplete((_, _) -> graphPresenter.setBusy(false));
        });
    }

    @Override
    public MainView getView() {
        return view;
    }

    private CompletableFuture<TypedObject> showObjectInfo(int groupId, int objectIndex) {
        var future = submit(groupId, objectIndex);
        return future.whenComplete((object, exception) -> {
            if (object != null) {
                SwingUtilities.invokeLater(() -> showObjectInfo(object, groupId, objectIndex));
            } else {
                log.error("Failed to read group {}", groupId, exception);
            }
        });
    }

    private void showObjectInfo(TypedObject object, int groupId, int objectIndex) {
        if (revealObjectInfo(groupId, objectIndex)) {
            return;
        }

        log.debug("Showing object info for {} (group: {}, index: {})", object.getType(), groupId, objectIndex);

        FlatTabbedPane pane = new FlatTabbedPane();
        pane.putClientProperty(PROP_GROUP_ID, groupId);
        pane.putClientProperty(PROP_OBJECT_INDEX, objectIndex);
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

        pane.add("Object", new JScrollPane(createObjectTree(game, object)));
        pane.setSelectedIndex(0);

        FlatTabbedPane tabs = view.getTabs();
        tabs.add(object.getType().toString(), pane);
        tabs.setToolTipTextAt(tabs.getTabCount() - 1, "Group: %d\nObject: %d".formatted(groupId, objectIndex));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private boolean revealObjectInfo(int groupId, int objectIndex) {
        var tabs = view.getTabs();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            var tab = (JComponent) tabs.getComponentAt(i);
            if (Objects.equals(groupId, tab.getClientProperty(PROP_GROUP_ID)) &&
                Objects.equals(objectIndex, tab.getClientProperty(PROP_OBJECT_INDEX))
            ) {
                tabs.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<TypedObject> submit(int groupId, int objectIndex) {
        var callable = (Callable<TypedObject>) () -> {
            log.debug("Reading group {}", groupId);
            var result = game.getStreamingReader().readGroup(groupId);
            var object = result.objects().get(objectIndex);
            return object.object();
        };
        return Futures.submit(callable);
    }

    private StructuredTree<?> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setTransferHandler(new ObjectTreeTransferHandler());
        tree.setLabelProvider(new TreeLabelProvider<>() {
            @Override
            public Optional<String> getText(ObjectStructure element) {
                return Optional.of(element.toString());
            }

            @Override
            public Optional<Icon> getIcon(ObjectStructure element) {
                return Optional.of(Fugue.getIcon("blue-document"));
            }
        });
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                showObjectInfo(link.get(), link.group(), link.index());
            }
        });
        Actions.installContextMenu(tree, ActionIds.OBJECT_MENU_ID, tree);
        return tree;
    }
}

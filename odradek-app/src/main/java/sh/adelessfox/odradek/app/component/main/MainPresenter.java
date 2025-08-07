package sh.adelessfox.odradek.app.component.main;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.app.component.graph.GraphPresenter;
import sh.adelessfox.odradek.app.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.app.mvvm.Presenter;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StructuredTreeModel;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

        eventBus.subscribe(GraphViewEvent.ObjectSelected.class, event -> {
            if (revealObjectInfo(event.groupId(), event.objectIndex())) {
                return;
            }
            graphPresenter.setBusy(true);
            var future = showObjectInfo(game, event.groupId(), event.objectIndex());
            future.whenComplete((_, _) -> graphPresenter.setBusy(false));
        });
    }

    @Override
    public MainView getView() {
        return view;
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
                    SwingUtilities.invokeLater(() -> showObjectInfo(object.type(), object.object(), groupId, objectIndex));
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

    private void showObjectInfo(ClassTypeInfo info, Object object, int groupId, int objectIndex) {
        if (revealObjectInfo(groupId, objectIndex)) {
            return;
        }

        log.debug("Showing object info for {} (group: {}, index: {})", info, groupId, objectIndex);

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

        pane.add("Object", new JScrollPane(createObjectTree(game, info, object)));
        pane.setSelectedIndex(0);

        FlatTabbedPane tabs = view.getTabs();
        tabs.add(info.toString(), pane);
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

    private StructuredTree<?> createObjectTree(Game game, ClassTypeInfo info, Object object) {
        var model = new StructuredTreeModel<>(new ObjectStructure.Compound(game, info, object));
        var tree = new StructuredTree<>(model);
        tree.setLargeModel(true);
        tree.setCellRenderer(new ObjectTreeCellRenderer());
        tree.setTransferHandler(new ObjectTreeTransferHandler());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                showObjectInfo(link.get().getType(), link.get(), link.group(), link.index());
            }
        });
        Actions.installContextMenu(tree, ActionIds.OBJECT_MENU_ID, tree);
        return tree;
    }
}

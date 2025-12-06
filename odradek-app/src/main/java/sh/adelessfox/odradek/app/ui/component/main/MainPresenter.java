package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.component.common.Presenter;
import sh.adelessfox.odradek.app.ui.component.graph.GraphPresenter;
import sh.adelessfox.odradek.app.ui.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.settings.SettingsEvent;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.EditorManager.Activation;
import sh.adelessfox.odradek.util.Futures;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class MainPresenter implements Presenter<MainView> {
    private static final Logger log = LoggerFactory.getLogger(MainPresenter.class);

    private final MainView view;
    private final GraphPresenter graphPresenter;
    private final EditorManager editorManager;
    private final ForbiddenWestGame game;

    @Inject
    public MainPresenter(
        ForbiddenWestGame game,
        GraphPresenter graphPresenter,
        EditorManager editorManager,
        MainView view,
        EventBus eventBus
    ) {
        this.view = view;
        this.game = game;
        this.graphPresenter = graphPresenter;
        this.editorManager = editorManager;

        eventBus.subscribe(GraphViewEvent.ShowObject.class, event -> showObject(event.groupId(), event.objectIndex()));
        eventBus.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) -> settings.objects().ifPresent(objects -> {
                    for (ObjectId object : objects) {
                        showObject(object.groupId(), object.objectIndex());
                    }
                });
                case SettingsEvent.BeforeSave(var settings) -> {
                    var objects = editorManager.getEditors().stream()
                        .map(Editor::getInput)
                        .gather(Gatherers.instanceOf(ObjectEditorInput.class))
                        .map(i -> new ObjectId(i.groupId(), i.objectIndex()))
                        .toList();
                    settings.objects().set(objects);
                }
            }
        });
    }

    public void showObject(int groupId, int objectIndex) {
        if (revealObjectInfo(groupId, objectIndex)) {
            return;
        }
        graphPresenter.setBusy(true);
        var future = showObjectInfo(groupId, objectIndex);
        future.whenComplete((_, _) -> graphPresenter.setBusy(false));
    }

    @Override
    public MainView getView() {
        return view;
    }

    private CompletableFuture<TypedObject> showObjectInfo(int groupId, int objectIndex) {
        var future = readObject(groupId, objectIndex);
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
        editorManager.openEditor(new ObjectEditorInput(game, object, groupId, objectIndex), Activation.REVEAL_AND_FOCUS);
    }

    private boolean revealObjectInfo(int groupId, int objectIndex) {
        Optional<Editor> editor = editorManager.findEditor(ei ->
            ei instanceof ObjectEditorInput(_, _, int groupId1, int objectIndex1)
                && groupId1 == groupId
                && objectIndex1 == objectIndex);
        editor.ifPresent(e -> editorManager.openEditor(e.getInput(), Activation.REVEAL_AND_FOCUS));
        return editor.isPresent();
    }

    private CompletableFuture<TypedObject> readObject(int groupId, int objectIndex) {
        return Futures.submit(() -> game.readObject(groupId, objectIndex));
    }
}

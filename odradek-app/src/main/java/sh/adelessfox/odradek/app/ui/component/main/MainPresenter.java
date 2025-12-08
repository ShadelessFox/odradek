package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.Presenter;
import sh.adelessfox.odradek.app.ui.component.graph.GraphViewEvent;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsEvent;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorManager;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MainPresenter implements Presenter<MainView> {
    private final MainView view;
    private final EditorManager editorManager;

    @Inject
    public MainPresenter(
        EditorManager editorManager,
        MainView view,
        EventBus eventBus
    ) {
        this.view = view;
        this.editorManager = editorManager;

        eventBus.subscribe(GraphViewEvent.ShowObject.class, event -> showObject(event.groupId(), event.objectIndex()));
        eventBus.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) -> loadEditors(settings);
                case SettingsEvent.BeforeSave(var settings) -> saveEditors(settings);
            }
        });
    }

    @Override
    public MainView getView() {
        return view;
    }

    private void showObject(int groupId, int objectIndex) {
        editorManager.openEditor(new ObjectEditorInputLazy(groupId, objectIndex));
    }

    private void loadEditors(Settings settings) {
        settings.objects().ifPresent(objects -> {
            for (ObjectId object : objects) {
                showObject(object.groupId(), object.objectIndex());
            }
        });
    }

    private void saveEditors(Settings settings) {
        var objects = new ArrayList<ObjectId>();
        for (Editor editor : editorManager.getEditors()) {
            switch (editor.getInput()) {
                case ObjectEditorInput i -> objects.add(new ObjectId(i.groupId(), i.objectIndex()));
                case ObjectEditorInputLazy i -> objects.add(new ObjectId(i.groupId(), i.objectIndex()));
                default -> { /* do nothing*/ }
            }
        }
        settings.objects().set(List.copyOf(objects));
    }
}

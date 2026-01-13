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
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer;

import java.util.ArrayList;

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

        eventBus.subscribe(GraphViewEvent.ShowObject.class, event -> openObject(event.groupId(), event.objectIndex()));
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

    private void openObject(int groupId, int objectIndex) {
        editorManager.openEditor(new ObjectEditorInputLazy(groupId, objectIndex));
    }

    private void loadEditors(Settings settings) {
        settings.editors().ifPresent(state -> loadContainer(editorManager.getRoot(), state));
    }

    private void saveEditors(Settings settings) {
        settings.editors().set(saveContainer(editorManager.getRoot()));
    }

    private void loadContainer(EditorStackContainer container, Settings.EditorState state) {
        switch (state) {
            case Settings.EditorState.Leaf leaf -> {
                var stack = container.getEditorStack();
                for (int i = 0; i < leaf.objects().size(); i++) {
                    var input = new ObjectEditorInputLazy(leaf.objects().get(i));
                    var select = i == leaf.selection();
                    editorManager.openEditor(input, stack, select ? EditorManager.Activation.REVEAL : EditorManager.Activation.NO);
                }
            }
            case Settings.EditorState.Split split -> {
                var result = container.split(split.orientation(), split.proportion(), false);
                loadContainer(result.left(), split.left());
                loadContainer(result.right(), split.right());
            }
        }
    }

    private Settings.EditorState saveContainer(EditorStackContainer container) {
        if (container.isSplit()) {
            var left = saveContainer(container.getLeftContainer());
            var right = saveContainer(container.getRightContainer());

            return new Settings.EditorState.Split(
                left,
                right,
                container.getOrientation(),
                container.getProportion()
            );
        } else {
            var stack = container.getEditorStack();
            var selected = stack.getSelectedEditor().orElse(null);

            var objects = new ArrayList<ObjectId>();
            int selection = 0;
            for (Editor editor : stack.getEditors()) {
                if (editor == selected) {
                    selection = objects.size();
                }
                switch (editor.getInput()) {
                    case ObjectEditorInput i -> objects.add(new ObjectId(i.groupId(), i.objectIndex()));
                    case ObjectEditorInputLazy i -> objects.add(new ObjectId(i.groupId(), i.objectIndex()));
                    default -> { /* do nothing*/ }
                }
            }

            return new Settings.EditorState.Leaf(
                objects,
                selection
            );
        }
    }
}

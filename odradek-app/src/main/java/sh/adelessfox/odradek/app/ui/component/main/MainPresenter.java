package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.Presenter;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer;

import java.util.ArrayList;
import java.util.Optional;

@Singleton
public class MainPresenter implements Presenter<MainView> {
    private final MainView view;
    private final EditorManager editorManager;

    @Inject
    MainPresenter(
        EditorManager editorManager,
        MainView view,
        EventBus eventBus
    ) {
        this.view = view;
        this.editorManager = editorManager;

        eventBus.subscribe(MainEvent.ShowObject.class, event -> openObject(event.objectId()));
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

    private void openObject(ObjectId objectId) {
        editorManager.openEditor(new ObjectEditorInputLazy(objectId));
    }

    private void loadEditors(Settings settings) {
        settings.get(ApplicationSettings.EDITORS).value().ifPresent(s -> loadContainer(editorManager.getRoot(), s));
    }

    private void saveEditors(Settings settings) {
        settings.get(ApplicationSettings.EDITORS).set(Optional.of(saveContainer(editorManager.getRoot())));
    }

    private void loadContainer(EditorStackContainer container, ApplicationSettings.EditorState state) {
        switch (state) {
            case ApplicationSettings.EditorState.Leaf leaf -> {
                var stack = container.getEditorStack();
                int selection = 0;
                for (int i = 0; i < leaf.editors().size(); i++) {
                    var tab = leaf.editors().get(i);
                    if (tab.history().isEmpty()) {
                        continue;
                    }
                    if (i == leaf.selection()) {
                        selection = stack.getTabCount();
                    }
                    var inputs = tab.history().stream()
                        .map(ObjectEditorInputLazy::new)
                        .toList();
                    var history = new EditorManager.History(inputs, Math.clamp(tab.selection(), 0, inputs.size() - 1));
                    editorManager.openEditorInNewTab(history, stack, EditorManager.Activation.NO);
                }
                if (stack.getTabCount() > 0) {
                    stack.setSelectedIndex(selection);
                }
            }
            case ApplicationSettings.EditorState.Split split -> {
                var result = container.split(split.orientation(), split.proportion(), false);
                loadContainer(result.left(), split.left());
                loadContainer(result.right(), split.right());
            }
        }
    }

    private ApplicationSettings.EditorState saveContainer(EditorStackContainer container) {
        if (container.isSplit()) {
            var left = saveContainer(container.getLeftContainer());
            var right = saveContainer(container.getRightContainer());

            return new ApplicationSettings.EditorState.Split(
                left,
                right,
                container.getOrientation(),
                container.getProportion()
            );
        } else {
            var stack = container.getEditorStack();
            var selected = stack.getSelectedEditor().orElse(null);

            var tabs = new ArrayList<ApplicationSettings.EditorState.Editor>();
            int selection = 0;
            for (Editor editor : stack.getEditors()) {
                if (!(editor.getInput() instanceof ObjectIdHolder)) {
                    continue;
                }
                if (editor == selected) {
                    selection = tabs.size();
                }
                var history = editorManager.getHistory(editor).orElseThrow();
                var objects = new ArrayList<ObjectId>();
                int current = 0;
                for (int i = 0; i < history.inputs().size(); i++) {
                    if (history.inputs().get(i) instanceof ObjectIdHolder holder) {
                        if (i == history.selection()) {
                            current = objects.size();
                        }
                        objects.add(holder.objectId());
                    }
                }
                tabs.add(new ApplicationSettings.EditorState.Editor(objects, current));
            }

            return new ApplicationSettings.EditorState.Leaf(
                tabs,
                selection
            );
        }
    }
}

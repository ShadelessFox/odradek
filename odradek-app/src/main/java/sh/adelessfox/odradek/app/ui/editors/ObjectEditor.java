package sh.adelessfox.odradek.app.ui.editors;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import sh.adelessfox.odradek.game.*;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorSite;
import sh.adelessfox.odradek.ui.util.Icons;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ObjectEditor implements Editor, ObjectHolder, ObjectIdHolder, DataContext {
    public static final class Provider implements Editor.Provider {
        @Override
        public Editor createEditor(EditorInput input, EditorSite site) {
            return new ObjectEditor((ObjectEditorInput) input);
        }

        @Override
        public Match matches(EditorInput input) {
            return input instanceof ObjectEditorInput ? Match.PRIMARY : Match.NONE;
        }
    }

    private final ObjectEditorInput input;
    private final List<Viewer> viewers = new ArrayList<>();

    private FlatTabbedPane pane;
    private Viewer lastViewer;

    private ObjectEditor(ObjectEditorInput input) {
        this.input = input;
    }

    @Override
    public JComponent createComponent() {
        if (pane == null) {
            pane = createRoot(input.game(), input.object());
        }
        return pane;
    }

    @Override
    public EditorInput getInput() {
        return input;
    }

    @Override
    public boolean isFocused() {
        return pane.getSelectedComponent().isFocusOwner();
    }

    @Override
    public void setFocus() {
        pane.getSelectedComponent().requestFocusInWindow();
    }

    @Override
    public void activate() {
        if (lastViewer != null) {
            lastViewer.activate();
        }
    }

    @Override
    public void deactivate() {
        if (lastViewer != null) {
            lastViewer.deactivate();
        }
    }

    @Override
    public void dispose() {
        if (lastViewer != null) {
            lastViewer.deactivate();
        }
        for (Viewer viewer : viewers) {
            viewer.dispose();
        }
    }

    @Override
    public TypedObject readObject(Game game) {
        if (input.game() != game) {
            throw new IllegalStateException("Unexpected game type");
        }
        return input.object();
    }

    @Override
    public ClassTypeInfo objectType() {
        return input.object().getType();
    }

    @Override
    public String objectName() {
        return "%s_%s_%s".formatted(objectType().name(), input.groupId(), input.objectIndex());
    }

    @Override
    public ObjectId objectId() {
        return new ObjectId(input.groupId(), input.objectIndex());
    }

    @Override
    public Optional<?> get(String key) {
        if (DataKeys.GAME.is(key)) {
            return Optional.of(input.game());
        }
        if (DataKeys.SELECTION_LIST.is(key)) {
            return Optional.of(List.of(this));
        }
        return Optional.empty();
    }

    private FlatTabbedPane createRoot(Game game, TypedObject object) {
        var pane = new FlatTabbedPane();
        pane.setTabPlacement(SwingConstants.BOTTOM);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        pane.setLeadingComponent(Actions.createToolBar(ObjectEditorActionIds.TOOLBAR_ID, this));
        pane.addChangeListener(_ -> {
            if (lastViewer != null) {
                lastViewer.deactivate();
            }
            lastViewer = viewers.get(pane.getSelectedIndex());
            lastViewer.activate();
        });

        Converter.converters(object.getType()).forEach(converter -> {
            Viewer.providers(converter.outputType()).forEach(provider -> {
                var result = converter.convert(object, game);
                if (result.isEmpty()) {
                    return;
                }

                var viewer = provider.create(result.get(), game);

                viewers.add(viewer);
                pane.insertTab(
                    provider.name(),
                    provider.icon().flatMap(Icons::getIconFromUri).orElse(null),
                    viewer.createComponent(),
                    null,
                    pane.getTabCount()
                );
            });
        });

        pane.setSelectedIndex(0);

        return pane;
    }
}

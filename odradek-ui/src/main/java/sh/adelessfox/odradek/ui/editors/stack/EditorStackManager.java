package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.EditorSite;
import sh.adelessfox.odradek.ui.editors.actions.EditorActionIds;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EditorStackManager implements EditorManager {
    private final EditorStackContainer root;
    private final EditorSite sharedSite = new MyEditorSite();

    public EditorStackManager() {
        root = new EditorStackContainer(this, createStack());
        root.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    root.removeHierarchyListener(this);
                    root.layoutContainer();
                }
            }
        });
    }

    @Override
    public void openEditor(EditorInput input) {
        openEditor(input, Activation.REVEAL_AND_FOCUS);
    }

    @Override
    public void openEditor(EditorInput input, Activation activation) {
        openEditor(input, findEditorStack(root), activation);
    }

    @Override
    public void openEditor(EditorInput input, EditorStack stack, Activation activation) {
        EditorComponent component = findEditorComponent(e -> input.representsSameInput(e.getInput())).orElse(null);

        if (component == null) {
            var result = createEditorForInput(input);
            var editor = result.editor();
            var provider = result.provider();

            component = new EditorComponent(activation == Activation.NO ? null : editor.createComponent(), editor, provider);
            stack.insertEditor(input, component, stack.getSelectedIndex() + 1);
        } else {
            // Do we have to check whether this editor belongs to the given stack?
            stack = component.getEditorStack();
        }

        if (activation != Activation.NO && stack.getSelectedComponent() != component) {
            // Prevents focus from being transferred if not required
            stack.setFocusable(false);
            stack.setSelectedComponent(component);
            stack.setFocusable(true);
        }

        if (activation == Activation.REVEAL_AND_FOCUS) {
            component.editor.setFocus();
        }
    }

    @Override
    public void openEditor(Editor oldEditor, EditorInput newInput) {
        EditorComponent oldComponent = findEditorComponent(e -> e.equals(oldEditor)).orElse(null);

        if (oldComponent != null) {
            EditorStack stack = (EditorStack) oldComponent.getParent();

            if (stack != null) {
                var selected = stack.getSelectedComponent() == oldComponent;
                var result = createEditorForInput(newInput);

                var newComponent = new EditorComponent(
                    selected ? result.editor().createComponent() : null,
                    result.editor(),
                    result.provider()
                );

                stack.reopenEditor(oldComponent, newInput, newComponent);
            }
        }
    }

    @Override
    public Optional<Editor> findEditor(Predicate<EditorInput> predicate) {
        return findEditorComponent(e -> predicate.test(e.getInput())).map(e -> e.editor);
    }

    @Override
    public Optional<Editor> findEditor(EditorInput input) {
        return findEditorComponent(e -> input.representsSameInput(e.getInput())).map(e -> e.editor);
    }

    @Override
    public Optional<Editor> findEditor(JComponent component) {
        if (component instanceof EditorComponent ec) {
            return Optional.of(ec.editor);
        }
        EditorComponent ec = (EditorComponent) SwingUtilities.getAncestorOfClass(EditorComponent.class, component);
        if (ec != null) {
            return Optional.of(ec.editor);
        }
        return Optional.empty();
    }

    @Override
    public List<Editor> getEditors() {
        List<Editor> editors = new ArrayList<>();
        forEachEditor(root, component -> {
            editors.add(component.editor);
            return Optional.empty();
        });
        return List.copyOf(editors);
    }

    @Override
    public List<Editor> getEditors(EditorStack stack) {
        List<Editor> editors = new ArrayList<>();
        forEachEditor(stack, component -> {
            editors.add(component.editor);
            return Optional.empty();
        });
        return List.copyOf(editors);
    }

    @Override
    public void closeEditor(Editor editor) {
        findEditorComponent(e -> e.equals(editor)).ifPresent(component -> {
            EditorStack stack = component.getEditorStack();
            stack.remove(component);

            if (component.hasComponent()) {
                component.setComponent(null);
                editor.dispose();
            }
        });
    }

    @Override
    public EditorStackContainer getRoot() {
        return root;
    }

    EditorStack createStack() {
        EditorStack stack = new EditorStack(this);
        stack.addChangeListener(_ -> {
            EditorComponent component = (EditorComponent) stack.getSelectedComponent();
            if (component != null && !component.hasComponent()) {
                component.setComponent(component.editor.createComponent());
            }
        });
        Actions.installContextMenu(stack, EditorActionIds.MENU_ID, key -> {
            if (DataKeys.EDITOR_MANAGER.is(key)) {
                return Optional.of(this);
            }
            if (DataKeys.EDITOR_STACK.is(key)) {
                return Optional.of(stack);
            }
            if (DataKeys.EDITOR.is(key)) {
                return getSelectedEditor(stack).map(e -> e.editor);
            }
            if (DataKeys.SELECTION_LIST.is(key)) {
                return getSelectedEditor(stack).map(e -> List.of(e.editor));
            }
            if (getSelectedEditor(stack).map(e -> e.editor).orElse(null) instanceof DataContext context) {
                return context.get(key);
            }
            return Optional.empty();
        });
        return stack;
    }

    private static Optional<EditorComponent> getSelectedEditor(EditorStack stack) {
        return Optional.ofNullable(stack.getSelectedComponent()).map(EditorComponent.class::cast);
    }

    private EditorStack findEditorStack(EditorStackContainer container) {
        if (container.isLeaf()) {
            return container.getEditorStack();
        } else {
            return findEditorStack(container.getLeftContainer());
        }
    }

    private Optional<EditorComponent> findEditorComponent(Predicate<Editor> predicate) {
        return forEachEditor(root, component -> {
            if (predicate.test(component.editor)) {
                return Optional.of(component);
            }
            return Optional.empty();
        });
    }

    private EditorResult createEditorForInput(EditorInput input) {
        var providers = Editor.providers(input).toList();

        Exception exception = null;

        for (Editor.Provider provider : providers) {
            try {
                return new EditorResult(provider.createEditor(input, sharedSite), provider);
            } catch (Exception e) {
                exception = e;
            }
        }

        throw new IllegalArgumentException("Unable to find a suitable editor for input: " + input, exception);
    }

    private <T> Optional<T> forEachEditor(EditorStackContainer container, Function<EditorComponent, Optional<T>> terminator) {
        return forEachStack(container, stack -> forEachEditor(stack, terminator));
    }

    private <T> Optional<T> forEachEditor(EditorStack stack, Function<EditorComponent, Optional<T>> terminator) {
        for (int i = 0; i < stack.getTabCount(); i++) {
            var component = (EditorComponent) stack.getComponentAt(i);
            var result = terminator.apply(component);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private <T> Optional<T> forEachStack(EditorStackContainer container, Function<EditorStack, Optional<T>> terminator) {
        if (container.isLeaf()) {
            return terminator.apply(container.getEditorStack());
        }

        var left = forEachStack(container.getLeftContainer(), terminator);
        if (left.isPresent()) {
            return left;
        }

        return forEachStack(container.getRightContainer(), terminator);
    }

    private record EditorResult(Editor editor, Editor.Provider provider) {
    }

    private class MyEditorSite implements EditorSite {
        @Override
        public EditorManager getManager() {
            return EditorStackManager.this;
        }
    }
}

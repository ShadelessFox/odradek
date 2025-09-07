package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.EditorProvider;
import sh.adelessfox.odradek.ui.editors.actions.EditorActionIds;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class EditorStackManager implements EditorManager {
    private final EditorStackContainer root;

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
    public Editor openEditor(EditorInput input) {
        return openEditor(input, Activation.REVEAL_AND_FOCUS);
    }

    @Override
    public Editor openEditor(EditorInput input, Activation activation) {
        EditorComponent component = findEditorComponent(e -> input.equals(e.getInput())).orElse(null);
        EditorStack stack;

        if (component == null) {
            var result = createEditorForInput(input);
            var editor = result.editor();
            var provider = result.provider();

            component = new EditorComponent(activation == Activation.NO ? null : editor.createComponent(), editor, provider);
            stack = findEditorStack(root);
            stack.insertTab(input.getName(), null, component, input.getDescription(), stack.getSelectedIndex() + 1);
        } else {
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

        return component.editor;
    }

    @Override
    public Optional<Editor> findEditor(Predicate<EditorInput> predicate) {
        return findEditorComponent(e -> predicate.test(e.getInput())).map(e -> e.editor);
    }

    @Override
    public Optional<Editor> findEditor(EditorInput input) {
        return findEditorComponent(e -> input.equals(e.getInput())).map(e -> e.editor);
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

    public EditorStackContainer getRoot() {
        return root;
    }

    EditorStack createStack() {
        EditorStack stack = new EditorStack(this);
        Actions.installContextMenu(stack, EditorActionIds.MENU_ID, key -> {
            if (DataKeys.EDITOR_MANAGER.is(key)) {
                return Optional.of(this);
            }
            if (DataKeys.EDITOR_STACK.is(key)) {
                return Optional.of(stack);
            }
            if (DataKeys.EDITOR.is(key)) {
                EditorComponent component = (EditorComponent) stack.getSelectedComponent();
                return Optional.of(component.editor);
            }
            return Optional.empty();
        });
        return stack;
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
        var providers = EditorProvider.providers(input).toList();

        Exception exception = null;

        for (EditorProvider provider : providers) {
            try {
                return new EditorResult(provider.createEditor(input), provider);
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

    private static class EditorComponent extends JComponent {
        private final Editor editor;
        private final EditorProvider provider;

        public EditorComponent(JComponent component, Editor editor, EditorProvider provider) {
            this.editor = editor;
            this.provider = provider;
            setLayout(new BorderLayout());
            setComponent(component);
        }

        EditorStack getEditorStack() {
            return (EditorStack) getParent();
        }

        boolean hasComponent() {
            BorderLayout layout = (BorderLayout) getLayout();
            return layout.getLayoutComponent(BorderLayout.CENTER) != null;
        }

        void setComponent(JComponent component) {
            setComponent(component, BorderLayout.CENTER);
        }

        private void setComponent(JComponent component, Object constraint) {
            var layout = (BorderLayout) getLayout();
            var currentComponent = layout.getLayoutComponent(constraint);
            boolean needsValidation = false;

            if (currentComponent != null) {
                remove(currentComponent);
                needsValidation = true;
            }

            if (component != null) {
                add(component, constraint);
                needsValidation = true;
            }

            if (needsValidation) {
                validate();
            }
        }
    }

    private record EditorResult(Editor editor, EditorProvider provider) {
    }
}

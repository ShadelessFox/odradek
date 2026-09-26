package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.EditorSite;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;
import sh.adelessfox.odradek.ui.editors.stack.dnd.EditorStackDropOverlay;
import sh.adelessfox.odradek.ui.editors.stack.dnd.EditorStackDropTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EditorStackManager implements EditorManager {
    private static final Integer OVERLAY_LAYER = JLayeredPane.POPUP_LAYER - 1;

    private final EditorStackContainer root;
    private final EditorSite sharedSite = () -> this;

    private EditorStackDropOverlay overlay;

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
        openEditor(input, findActiveEditorStack(), activation);
    }

    @Override
    public void openEditor(EditorInput input, EditorStack stack, Activation activation) {
        var history = findEditorHistory(e -> input.representsSameInput(e.getInput())).orElse(null);
        if (history == null) {
            openEditorInNewTab(input, stack, activation);
        } else {
            reveal(history, activation);
        }
    }

    @Override
    public void openEditorInNewTab(EditorInput input) {
        openEditorInNewTab(input, findActiveEditorStack(), Activation.REVEAL_AND_FOCUS);
    }

    @Override
    public void openEditorInNewTab(EditorInput input, EditorStack stack, Activation activation) {
        openEditorInNewTab(new History(List.of(input), 0), stack, activation);
    }

    @Override
    public void openEditorInNewTab(History state, EditorStack stack, Activation activation) {
        var entries = state.inputs().stream().map(this::createEditorComponent).toList();
        var history = new EditorHistory(entries, state.selection());
        int index = activation == Activation.NO ? stack.getTabCount() : stack.getSelectedIndex() + 1;
        stack.insertEditor(history.current().editor.getInput(), history, index);
        reveal(history, activation);
    }

    @Override
    public Optional<History> getHistory(Editor editor) {
        return findEditorHistory(e -> e == editor).map(EditorHistory::snapshot);
    }

    private static void reveal(EditorHistory history, Activation activation) {
        var stack = history.getEditorStack();
        if (activation != Activation.NO && stack.getSelectedComponent() != history) {
            // Prevents focus from being transferred if not required
            stack.setFocusable(false);
            stack.setSelectedComponent(history);
            stack.setFocusable(true);
        }

        if (activation == Activation.REVEAL_AND_FOCUS) {
            history.current().editor.setFocus();
        }
    }

    @Override
    public void navigate(Editor source, EditorInput input) {
        findEditorHistory(e -> e == source).ifPresent(e -> e.navigate(createEditorComponent(input)));
    }

    @Override
    public void openEditor(Editor oldEditor, EditorInput newInput) {
        forEachEditor(root, history -> history.find(oldEditor).map(entry -> {
            history.replace(entry, createEditorComponent(newInput));
            return true;
        }));
    }

    @Override
    public Optional<Editor> findEditor(Predicate<EditorInput> predicate) {
        return findEditorHistory(e -> predicate.test(e.getInput())).map(e -> e.current().editor);
    }

    @Override
    public Optional<Editor> findEditor(EditorInput input) {
        return findEditorHistory(e -> input.representsSameInput(e.getInput())).map(e -> e.current().editor);
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
        if (component instanceof EditorHistory history) {
            return Optional.of(history.current().editor);
        }
        var history = (EditorHistory) SwingUtilities.getAncestorOfClass(EditorHistory.class, component);
        if (history != null) {
            return Optional.of(history.current().editor);
        }
        return Optional.empty();
    }

    @Override
    public List<Editor> getEditors() {
        List<Editor> editors = new ArrayList<>();
        forEachEditor(root, component -> {
            editors.add(component.current().editor);
            return Optional.empty();
        });
        return List.copyOf(editors);
    }

    @Override
    public List<Editor> getEditors(EditorStack stack) {
        List<Editor> editors = new ArrayList<>();
        forEachEditor(stack, component -> {
            editors.add(component.current().editor);
            return Optional.empty();
        });
        return List.copyOf(editors);
    }

    @Override
    public void closeEditor(Editor editor) {
        findEditorHistory(e -> e == editor).ifPresent(history -> {
            var stack = history.getEditorStack();
            stack.remove(history);
            history.dispose();
        });
    }

    @Override
    public EditorStackContainer getRoot() {
        return root;
    }

    EditorStack createStack() {
        EditorStack stack = new EditorStack(this);
        Actions.installContextMenu(stack, EditorMenu.ID, key -> {
            if (DataKeys.EDITOR_MANAGER.is(key)) {
                return Optional.of(this);
            }
            if (DataKeys.EDITOR_STACK.is(key)) {
                return Optional.of(stack);
            }
            if (DataKeys.EDITOR.is(key) || DataKeys.SELECTION.is(key)) {
                return getSelectedEditor(stack).map(e -> e.current().editor);
            }
            if (DataKeys.SELECTION_LIST.is(key)) {
                return getSelectedEditor(stack).map(e -> List.of(e.current().editor));
            }
            if (getSelectedEditor(stack).map(e -> e.current().editor).orElse(null) instanceof DataContext context) {
                return context.get(key);
            }
            return Optional.empty();
        });

        var listener = new EditorDragSourceListener(stack);
        stack.addMouseListener(listener);
        stack.addMouseMotionListener(listener);

        return stack;
    }

    private static Optional<EditorHistory> getSelectedEditor(EditorStack stack) {
        return Optional.ofNullable(stack.getSelectedComponent()).map(EditorHistory.class::cast);
    }

    private EditorStack findActiveEditorStack() {
        var focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        var focusOwner = focusManager.getPermanentFocusOwner();
        if (focusOwner instanceof EditorStack stack) {
            return stack;
        }
        var stack = (EditorStack) SwingUtilities.getAncestorOfClass(EditorStack.class, focusOwner);
        if (stack != null) {
            return stack;
        }
        return findEditorStack(root);
    }

    private EditorStack findEditorStack(EditorStackContainer container) {
        if (container.isLeaf()) {
            return container.getEditorStack();
        } else {
            return findEditorStack(container.getLeftContainer());
        }
    }

    private Optional<EditorHistory> findEditorHistory(Predicate<Editor> predicate) {
        return forEachEditor(root, component -> {
            if (predicate.test(component.current().editor)) {
                return Optional.of(component);
            }
            return Optional.empty();
        });
    }

    private EditorComponent createEditorComponent(EditorInput input) {
        var providers = Editor.providers(input).toList();

        Exception exception = null;

        for (Editor.Provider provider : providers) {
            try {
                return new EditorComponent(provider.createEditor(input, sharedSite), provider);
            } catch (Exception e) {
                exception = e;
            }
        }

        throw new IllegalArgumentException("Unable to find a suitable editor for input: " + input, exception);
    }

    private <T> Optional<T> forEachEditor(
        EditorStackContainer container,
        Function<EditorHistory, Optional<T>> terminator
    ) {
        return forEachStack(container, stack -> forEachEditor(stack, terminator));
    }

    private <T> Optional<T> forEachEditor(EditorStack stack, Function<EditorHistory, Optional<T>> terminator) {
        for (int i = 0; i < stack.getTabCount(); i++) {
            var component = (EditorHistory) stack.getComponentAt(i);
            var result = terminator.apply(component);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    private <T> Optional<T> forEachStack(
        EditorStackContainer container,
        Function<EditorStack, Optional<T>> terminator
    ) {
        if (container.isLeaf()) {
            return terminator.apply(container.getEditorStack());
        }

        var left = forEachStack(container.getLeftContainer(), terminator);
        if (left.isPresent()) {
            return left;
        }

        return forEachStack(container.getRightContainer(), terminator);
    }

    private EditorStackDropOverlay obtainOverlay() {
        var rootPane = root.getRootPane();

        if (overlay == null) {
            overlay = new EditorStackDropOverlay(this);
            rootPane.getLayeredPane().add(overlay, OVERLAY_LAYER);
        }

        Insets insets = rootPane.getInsets();
        Rectangle bounds = new Rectangle(
            SwingUtilities.convertPoint(root, -insets.left, -insets.top, rootPane),
            root.getSize()
        );
        overlay.setBounds(bounds);

        return overlay;
    }

    private class EditorDragSourceListener extends MouseAdapter {
        private final EditorStack stack;
        private int index;

        EditorDragSourceListener(EditorStack stack) {
            this.stack = stack;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                index = stack.indexAtLocation(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (index < 0 || !SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            var overlay = obtainOverlay();
            overlay.setVisible(true);
            overlay.update(stack, e.getXOnScreen(), e.getYOnScreen());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (index < 0 || !SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            var overlay = obtainOverlay();
            overlay.setVisible(false);
            overlay.getTarget().ifPresent(result -> {
                switch (result) {
                    case EditorStackDropTarget.Move(var target, int targetIndex) ->
                        stack.move(stack.getEditorAt(index), target, targetIndex);
                    case EditorStackDropTarget.Split(var target, var targetPosition) ->
                        stack.move(stack.getEditorAt(index), target, targetPosition);
                }
            });
            overlay.release();
        }
    }
}

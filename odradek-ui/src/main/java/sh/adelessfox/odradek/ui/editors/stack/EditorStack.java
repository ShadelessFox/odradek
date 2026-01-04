package sh.adelessfox.odradek.ui.editors.stack;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer.Orientation;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;

/**
 * A holder for one or more editors grouped together.
 */
public class EditorStack extends FlatTabbedPane {
    public enum Position {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        CENTER
    }

    private final EditorStackManager manager;
    private EditorComponent lastEditor;

    EditorStack(EditorStackManager manager) {
        this.manager = manager;

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        setTabsClosable(true);
        setTabCloseToolTipText("Close");
        setTabCloseCallback((_, index) -> {
            var component = (JComponent) getComponentAt(index);
            var editor = manager.findEditor(component).orElseThrow();
            manager.closeEditor(editor);
        });

        getModel().addChangeListener(_ -> {
            // Deactivate last editor
            if (lastEditor != null && lastEditor.hasComponent()) {
                lastEditor.editor.deactivate();
            }

            // Retrieve new editor. If it doesn't exist, then this stack is empty; otherwise, activate
            lastEditor = (EditorComponent) getSelectedComponent();
            if (lastEditor == null) {
                getContainer().compact();
            } else if (lastEditor.hasComponent()) {
                lastEditor.editor.activate();
            }
        });

        addMouseListener(new MouseAdapter() {
            private int lastPressedIndex;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    lastPressedIndex = indexAtLocation(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) && lastPressedIndex >= 0 && lastPressedIndex == indexAtLocation(e.getX(), e.getY())) {
                    var component = (JComponent) getComponentAt(lastPressedIndex);
                    var editor = manager.findEditor(component).orElseThrow();
                    manager.closeEditor(editor);
                    lastPressedIndex = -1;
                }
            }
        });
    }

    void insertEditor(EditorInput input, EditorComponent component, int index) {
        insertTab(input.getName(), null, component, input.getDescription(), index);
    }

    void reopenEditor(EditorComponent oldComponent, EditorInput newInput, EditorComponent newComponent) {
        int index = indexOfComponent(oldComponent);
        boolean selected = getSelectedIndex() == index;

        if (index < 0) {
            throw new IllegalArgumentException("Old component is not in this stack");
        }

        Editor oldEditor = oldComponent.editor;
        Editor newEditor = newComponent.editor;

        if (oldComponent.hasComponent()) {
            if (selected) {
                oldEditor.deactivate();
            }
            oldEditor.dispose();
        }

        setComponentAt(index, newComponent);
        setTitleAt(index, newInput.getName());
        setToolTipTextAt(index, newInput.getDescription());

        if (selected) {
            lastEditor = newComponent;
            newEditor.activate();

            if (oldEditor.isFocused()) {
                // Restore focus
                newEditor.setFocus();
            }
        }
    }

    public boolean move(Editor sourceEditor, EditorStack targetStack) {
        for (int i = 0; i < getTabCount(); i++) {
            var component = (JComponent) getComponentAt(i);
            var editor = manager.findEditor(component).orElseThrow();

            if (editor == sourceEditor) {
                return move(this, i, targetStack, targetStack.getTabCount());
            }
        }

        return false;
    }

    public boolean move(Editor sourceEditor, EditorStack targetStack, Position position) {
        for (int i = 0; i < getTabCount(); i++) {
            var component = (JComponent) getComponentAt(i);
            var editor = manager.findEditor(component).orElseThrow();

            if (editor == sourceEditor) {
                return move(this, i, targetStack, position);
            }
        }

        return false;
    }

    private boolean move(EditorStack source, int sourceIndex, EditorStack target, Position targetPosition) {
        if (source == target && target.getTabCount() < 2) {
            return false;
        }

        EditorStack destination = switch (targetPosition) {
            case CENTER -> target;
            case TOP -> target.getContainer().split(Orientation.VERTICAL, 0.5, true).targetStack();
            case BOTTOM -> target.getContainer().split(Orientation.VERTICAL, 0.5, false).targetStack();
            case LEFT -> target.getContainer().split(Orientation.HORIZONTAL, 0.5, true).targetStack();
            case RIGHT -> target.getContainer().split(Orientation.HORIZONTAL, 0.5, false).targetStack();
        };

        return move(source, sourceIndex, destination, destination.getTabCount());
    }

    private boolean move(EditorStack source, int sourceIndex, EditorStack target, int targetIndex) {
        Objects.checkIndex(sourceIndex, source.getTabCount());
        Objects.checkIndex(targetIndex, target.getTabCount() + 1);

        if (source == target && sourceIndex == targetIndex) {
            // No-op
            return false;
        }

        var title = source.getTitleAt(sourceIndex);
        var icon = source.getIconAt(sourceIndex);
        var tooltip = source.getToolTipTextAt(sourceIndex);
        var component = source.getComponentAt(sourceIndex);

        source.remove(sourceIndex);

        if (source != target) {
            // Between different stacks
            target.insertTab(title, icon, component, tooltip, targetIndex);
            target.setSelectedIndex(targetIndex);
        } else if (targetIndex == source.getTabCount()) {
            // Within the same stack, the last tab
            target.insertTab(title, icon, component, tooltip, target.getTabCount());
            target.setSelectedIndex(target.getTabCount() - 1);
        } else if (sourceIndex > targetIndex) {
            // Within the same stack, before the source tab
            target.insertTab(title, icon, component, tooltip, targetIndex);
            target.setSelectedIndex(targetIndex);
        } else {
            // Within the same stack, after the source tab
            target.insertTab(title, icon, component, tooltip, targetIndex - 1);
            target.setSelectedIndex(targetIndex - 1);
        }

        getContainer().compact();
        return true;
    }

    public EditorStackContainer getContainer() {
        return (EditorStackContainer) getParent();
    }

    public Optional<EditorStack> getOpposite() {
        EditorStackContainer container = getContainer();
        if (!container.isLeaf()) {
            return Optional.empty();
        }
        Optional<EditorStackContainer> opposite = container.getOpposite(container);
        return opposite
            .filter(EditorStackContainer::isLeaf)
            .map(EditorStackContainer::getEditorStack);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!(getParent() instanceof EditorStackContainer)) {
            throw new IllegalStateException("Illegal parent of an editor stack");
        }
    }
}

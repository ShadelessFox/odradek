package sh.adelessfox.odradek.ui.editors.stack;

import com.formdev.flatlaf.FlatClientProperties;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer.Orientation;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * A holder for one or more editors grouped together.
 */
public class EditorStack extends JTabbedPane {
    public enum Position {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        CENTER
    }

    EditorStack(EditorStackManager manager) {
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (IntConsumer) index -> {
            var component = (JComponent) getComponentAt(index);
            var editor = manager.findEditor(component).orElseThrow();
            manager.closeEditor(editor);
        });

        getModel().addChangeListener(_ -> {
            if (getTabCount() == 0) {
                getContainer().compact();
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

    public boolean move(EditorStack source, int sourceIndex, EditorStack target, Position targetPosition) {
        if (source == target && target.getTabCount() < 2) {
            return false;
        }

        EditorStack destination = switch (targetPosition) {
            case CENTER -> target;
            case TOP -> target.getContainer().split(Orientation.HORIZONTAL, 0.5, true).targetStack();
            case BOTTOM -> target.getContainer().split(Orientation.HORIZONTAL, 0.5, false).targetStack();
            case LEFT -> target.getContainer().split(Orientation.VERTICAL, 0.5, true).targetStack();
            case RIGHT -> target.getContainer().split(Orientation.VERTICAL, 0.5, false).targetStack();
        };

        return move(source, sourceIndex, destination, destination.getTabCount());
    }

    public boolean move(EditorStack source, int sourceIndex, EditorStack target, int targetIndex) {
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

    @Override
    public void addNotify() {
        super.addNotify();
        if (!(getParent() instanceof EditorStackContainer)) {
            throw new IllegalStateException("Illegal parent of an editor stack");
        }
    }
}

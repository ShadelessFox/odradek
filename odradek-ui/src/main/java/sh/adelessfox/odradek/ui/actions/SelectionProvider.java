package sh.adelessfox.odradek.ui.actions;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;

interface SelectionProvider<T extends JComponent, R> {
    /**
     * Gets the selection for the given component.
     *
     * @param component the component to get the selection from
     * @param event     the event that triggered the selection request
     * @return an {@link Optional} containing the selection if present, otherwise empty
     */
    Optional<R> getSelection(T component, EventObject event);

    /**
     * Sets the selection for the given component.
     *
     * @param component the component to set the selection on
     * @param selection the selection to set
     * @param event     the event that triggered the selection change
     */
    void setSelection(T component, R selection, EventObject event);

    /**
     * Gets the location of the selection within the component.
     *
     * @param component the component to get the selection location from
     * @param selection the selection for which to get the location
     * @param event     the event that triggered the request
     * @return a {@link Point} representing the location of the selection within the component
     */
    Point getSelectionLocation(T component, R selection, EventObject event);

    static SelectionProvider<? super JTree, ? extends TreePath> treeSelection() {
        return new SelectionProvider<>() {
            @Override
            public Optional<TreePath> getSelection(JTree component, EventObject event) {
                if (event instanceof MouseEvent me) {
                    final TreePath path = component.getPathForLocation(me.getX(), me.getY());
                    if (path != null) {
                        return Optional.of(path);
                    }
                }
                return Optional.ofNullable(component.getSelectionPath());
            }

            @Override
            public void setSelection(JTree component, TreePath selection, EventObject event) {
                component.setSelectionPath(selection);
                if (!(event instanceof MouseEvent)) {
                    component.scrollPathToVisible(selection);
                }
            }

            @Override
            public Point getSelectionLocation(JTree component, TreePath selection, EventObject event) {
                if (event instanceof MouseEvent me) {
                    return me.getPoint();
                } else {
                    var bounds = Objects.requireNonNull(component.getPathBounds(selection));
                    return new Point(bounds.x, bounds.y + bounds.height);
                }
            }
        };
    }

    static SelectionProvider<? super JComponent, Object> componentSelection() {
        return new SelectionProvider<>() {
            @Override
            public Optional<Object> getSelection(JComponent component, EventObject event) {
                return Optional.of(component);
            }

            @Override
            public void setSelection(JComponent component, Object selection, EventObject event) {
                // do nothing
            }

            @Override
            public Point getSelectionLocation(JComponent component, Object selection, EventObject event) {
                if (event instanceof MouseEvent me) {
                    return me.getPoint();
                } else {
                    var bounds = component.getBounds();
                    return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
                }
            }
        };
    }
}

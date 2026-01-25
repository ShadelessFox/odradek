package sh.adelessfox.odradek.ui.editors.stack.dnd;

import sh.adelessfox.odradek.ui.editors.stack.EditorStack;

/**
 * A result of a drag-and-drop operation onto an {@link EditorStack}.
 *
 * @see EditorStackDropOverlay
 */
public sealed interface EditorStackDropTarget {
    EditorStack stack();

    record Move(EditorStack stack, int index) implements EditorStackDropTarget {
    }

    record Split(EditorStack stack, EditorStack.Position position) implements EditorStackDropTarget {
    }
}

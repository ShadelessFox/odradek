package sh.adelessfox.odradek.ui.editors;

import sh.adelessfox.odradek.ui.editors.stack.EditorStack;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public interface EditorManager {
    enum Activation {
        NO,
        REVEAL,
        REVEAL_AND_FOCUS
    }

    /** An immutable snapshot of a tab's inputs and its current history position. */
    record History(List<? extends EditorInput> inputs, int selection) {
        public History {
            inputs = List.copyOf(inputs);
            Objects.checkIndex(selection, inputs.size());
        }
    }

    EditorStackContainer getRoot();

    void openEditor(EditorInput input);

    void openEditor(EditorInput input, Activation activation);

    void openEditor(EditorInput input, EditorStack stack, Activation activation);

    /** Replaces an editor in its history entry, without navigating or changing tabs. */
    void openEditor(Editor editor, EditorInput input);

    /** Opens a new tab, even if another tab already represents this input. */
    void openEditorInNewTab(EditorInput input);

    /** Opens a new tab in the given stack. Unrevealed tabs are appended. */
    void openEditorInNewTab(EditorInput input, EditorStack stack, Activation activation);

    /** Restores a new tab's complete history, initializing entries only when visited. */
    void openEditorInNewTab(History history, EditorStack stack, Activation activation);

    /** Appends an input to the source editor's tab history. */
    void navigate(Editor source, EditorInput input);

    Optional<History> getHistory(Editor editor);

    Optional<Editor> findEditor(Predicate<EditorInput> predicate);

    Optional<Editor> findEditor(EditorInput input);

    Optional<Editor> findEditor(JComponent component);

    List<Editor> getEditors();

    List<Editor> getEditors(EditorStack stack);

    void closeEditor(Editor editor);
}

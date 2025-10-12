package sh.adelessfox.odradek.ui.editors;

import sh.adelessfox.odradek.ui.editors.stack.EditorStack;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface EditorManager {
    enum Activation {
        NO,
        REVEAL,
        REVEAL_AND_FOCUS
    }

    Editor openEditor(EditorInput input);

    Editor openEditor(EditorInput input, Activation activation);

    Optional<Editor> findEditor(Predicate<EditorInput> predicate);

    Optional<Editor> findEditor(EditorInput input);

    Optional<Editor> findEditor(JComponent component);

    List<Editor> getEditors();

    List<Editor> getEditors(EditorStack stack);

    void closeEditor(Editor editor);
}

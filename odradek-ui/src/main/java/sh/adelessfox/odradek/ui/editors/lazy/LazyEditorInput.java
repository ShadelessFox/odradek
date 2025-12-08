package sh.adelessfox.odradek.ui.editors.lazy;

import sh.adelessfox.odradek.ui.editors.EditorInput;

public interface LazyEditorInput extends EditorInput {
    EditorInput loadRealInput() throws Exception;

    boolean canLoadImmediately();

    LazyEditorInput canLoadImmediately(boolean value);
}

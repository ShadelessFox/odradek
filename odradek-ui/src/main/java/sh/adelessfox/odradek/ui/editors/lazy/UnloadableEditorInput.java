package sh.adelessfox.odradek.ui.editors.lazy;

import sh.adelessfox.odradek.ui.editors.EditorInput;

/**
 * An editor input can can be unloaded back to its lazy variant.
 *
 * @see LazyEditorInput
 */
public interface UnloadableEditorInput extends EditorInput {
    LazyEditorInput unloadInput();
}

package sh.adelessfox.odradek.app.editors;

import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorProvider;

public final class ObjectEditorProvider implements EditorProvider {
    @Override
    public Editor createEditor(EditorInput input) {
        return new ObjectEditor((ObjectEditorInput) input);
    }

    @Override
    public Match matches(EditorInput input) {
        return input instanceof ObjectEditorInput ? Match.PRIMARY : Match.NONE;
    }
}

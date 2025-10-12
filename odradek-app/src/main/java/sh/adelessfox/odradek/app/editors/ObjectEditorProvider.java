package sh.adelessfox.odradek.app.editors;

import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorProvider;
import sh.adelessfox.odradek.ui.editors.EditorSite;

public final class ObjectEditorProvider implements EditorProvider {
    @Override
    public Editor createEditor(EditorInput input, EditorSite site) {
        return new ObjectEditor((ObjectEditorInput) input, site);
    }

    @Override
    public Match matches(EditorInput input) {
        return input instanceof ObjectEditorInput ? Match.PRIMARY : Match.NONE;
    }
}

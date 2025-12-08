package sh.adelessfox.odradek.ui.editors.lazy;

import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorProvider;
import sh.adelessfox.odradek.ui.editors.EditorSite;

public class LazyEditorProvider implements EditorProvider {
    @Override
    public Editor createEditor(EditorInput input, EditorSite site) {
        return new LazyEditor((LazyEditorInput) input, site);
    }

    @Override
    public Match matches(EditorInput input) {
        return input instanceof LazyEditorInput ? Match.PRIMARY : Match.NONE;
    }
}

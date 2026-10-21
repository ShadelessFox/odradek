package sh.adelessfox.odradek.ui.editors;

import sh.adelessfox.odradek.data.DataContextOwner;

/** Editor host services. The context is shared amongst all editors. */
public interface EditorSite extends DataContextOwner {
    EditorManager getManager();
}

package sh.adelessfox.odradek.ui.editors;

import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;

public interface Editor extends Disposable, Focusable {
    JComponent createComponent();

    EditorInput getInput();

    @Override
    default void dispose() {
        // Do nothing by default
    }
}

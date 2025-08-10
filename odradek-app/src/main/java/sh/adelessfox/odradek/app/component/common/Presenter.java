package sh.adelessfox.odradek.app.component.common;

import javax.swing.*;

public interface Presenter<V extends View<?>> {
    V getView();

    default JComponent getRoot() {
        return getView().getRoot();
    }
}

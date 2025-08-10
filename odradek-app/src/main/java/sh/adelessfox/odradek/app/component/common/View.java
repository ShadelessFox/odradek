package sh.adelessfox.odradek.app.component.common;

import javax.swing.*;

public interface View<V extends JComponent> {
    V getRoot();
}

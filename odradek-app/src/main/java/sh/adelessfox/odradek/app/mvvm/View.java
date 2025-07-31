package sh.adelessfox.odradek.app.mvvm;

import javax.swing.*;

public interface View<V extends JComponent> {
    V getRoot();
}

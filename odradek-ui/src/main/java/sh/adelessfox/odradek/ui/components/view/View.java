package sh.adelessfox.odradek.ui.components.view;

import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;

public interface View extends Focusable {
    JComponent createComponent();
}

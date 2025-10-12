package sh.adelessfox.odradek.ui.components.toolwindow;

import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;

public interface ToolWindowPane extends Focusable {
    JComponent createComponent();
}

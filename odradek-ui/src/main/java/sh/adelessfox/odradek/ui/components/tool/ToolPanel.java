package sh.adelessfox.odradek.ui.components.tool;

import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;

public interface ToolPanel extends Focusable {
    JComponent createComponent();
}

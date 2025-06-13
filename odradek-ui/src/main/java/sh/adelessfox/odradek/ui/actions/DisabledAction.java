package sh.adelessfox.odradek.ui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

final class DisabledAction extends AbstractAction {
    public DisabledAction(String name) {
        super(name);
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}

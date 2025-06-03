package sh.adelessfox.odradek.ui.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
public interface DocumentAdapter extends DocumentListener {
    @Override
    default void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
}

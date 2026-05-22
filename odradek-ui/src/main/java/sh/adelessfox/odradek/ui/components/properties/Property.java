package sh.adelessfox.odradek.ui.components.properties;

import javax.swing.*;

sealed interface Property permits Property.Labeled, BooleanProperty {
    JComponent create();

    sealed interface Labeled extends Property permits FloatProperty {
        String label();
    }
}

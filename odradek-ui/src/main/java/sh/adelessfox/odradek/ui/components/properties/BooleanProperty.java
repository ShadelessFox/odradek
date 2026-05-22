package sh.adelessfox.odradek.ui.components.properties;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

record BooleanProperty(
    String label,
    Supplier<Boolean> getter,
    Consumer<Boolean> setter
) implements Property {
    @Override
    public JComponent create() {
        var input = new JCheckBox(label);
        input.setSelected(getter.get());
        input.addActionListener(_ -> setter.accept(input.isSelected()));
        return input;
    }
}

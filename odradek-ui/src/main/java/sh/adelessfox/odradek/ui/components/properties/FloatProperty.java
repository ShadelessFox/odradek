package sh.adelessfox.odradek.ui.components.properties;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

record FloatProperty(
    String label,
    Supplier<Float> getter,
    Consumer<Float> setter,
    float min,
    float max,
    float step
) implements Property.Labeled {
    @Override
    public JComponent create() {
        var model = new SpinnerNumberModel(Math.clamp(getter.get(), min, max), min, max, step);
        var input = new JSpinner(model);
        input.addChangeListener(_ -> setter.accept(model.getNumber().floatValue()));
        return input;
    }
}

package sh.adelessfox.odradek.ui.components.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PropertyCategoryBuilder {
    final String label;
    final List<Property> properties = new ArrayList<>();

    PropertyCategoryBuilder(String label) {
        this.label = label;
    }

    public PropertyCategoryBuilder property(
        String label,
        Supplier<Float> getter,
        Consumer<Float> setter,
        float min,
        float max,
        float step
    ) {
        properties.add(new FloatProperty(label, getter, setter, min, max, step));
        return this;
    }

    public PropertyCategoryBuilder property(
        String label,
        Supplier<Boolean> getter,
        Consumer<Boolean> setter
    ) {
        properties.add(new BooleanProperty(label, getter, setter));
        return this;
    }
}

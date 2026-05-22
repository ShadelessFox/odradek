package sh.adelessfox.odradek.ui.components.properties;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.components.LabeledSeparator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PropertyPanelBuilder {
    private final List<PropertyCategoryBuilder> categories = new ArrayList<>();

    private PropertyPanelBuilder() {
    }

    public static JComponent build(Consumer<PropertyPanelBuilder> handler) {
        var builder = new PropertyPanelBuilder();
        handler.accept(builder);
        return builder.build();
    }

    public PropertyPanelBuilder category(String label, Consumer<PropertyCategoryBuilder> handler) {
        var category = new PropertyCategoryBuilder(label);
        handler.accept(category);
        categories.add(category);
        return this;
    }

    public JComponent build() {
        var panel = new JPanel();
        panel.setLayout(new MigLayout("ins panel,wrap", "[fill,grow][]"));
        panel.setOpaque(false);

        for (var category : categories) {
            if (category.properties.isEmpty()) {
                throw new IllegalStateException("Category " + category.label + " has no properties");
            }

            panel.add(new LabeledSeparator(category.label), "span,growx");

            for (var property : category.properties) {
                if (property instanceof Property.Labeled labeled) {
                    panel.add(property.create(), "gap ind");
                    panel.add(new JLabel(labeled.label()));
                } else {
                    panel.add(property.create(), "gap ind,span");
                }
            }
        }

        return panel;
    }
}

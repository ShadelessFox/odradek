package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public abstract class StyledListCellRenderer<T> extends StyledComponent implements ListCellRenderer<T> {
    private static final Insets INSETS_LIST = new Insets(3, 6, 3, 6);
    private static final Insets INSETS_COMBO = new Insets(0, 6, 0, 6);

    private boolean selected;

    public static <T> StyledListCellRenderer<T> adapter(BiConsumer<T, StyledComponent> consumer) {
        return new StyledListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(
                JList<? extends T> list,
                T value,
                int index,
                boolean selected,
                boolean focused
            ) {
                consumer.accept(value, this);
            }
        };
    }

    @Override
    public Component getListCellRendererComponent(
        JList<? extends T> list,
        T value,
        int index,
        boolean isSelected,
        boolean cellHasFocus
    ) {
        clear();
        selected = isSelected;

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        if (list.getModel() instanceof ComboBoxModel<?>) {
            setPadding(INSETS_COMBO);
        } else {
            setPadding(INSETS_LIST);
        }

        customizeCellRenderer(list, value, index, isSelected, cellHasFocus);
        return this;
    }

    @Override
    public void append(StyledFragment fragment) {
        if (selected) {
            super.append(fragment.withForeground(getForeground()));
        } else {
            super.append(fragment);
        }
    }

    protected abstract void customizeCellRenderer(
        JList<? extends T> list,
        T value,
        int index,
        boolean selected,
        boolean focused
    );
}

package sh.adelessfox.odradek.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import java.awt.*;

public abstract class StyledListCellRenderer<T> extends StyledCellRenderer implements ListCellRenderer<T> {
    private static final Insets INSETS_LIST = new Insets(3, 6, 3, 6);
    private static final Insets INSETS_COMBO = new Insets(0, 6, 0, 6);

    private JList<?> list;
    private boolean selected;

    @Override
    public Component getListCellRendererComponent(
        JList<? extends T> list,
        T value,
        int index,
        boolean isSelected,
        boolean cellHasFocus
    ) {
        this.list = list;
        this.selected = isSelected;

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

        setText(getText(list, value, index, isSelected, cellHasFocus));
        return this;
    }

    @Override
    protected boolean isSelected() {
        return selected;
    }

    @Override
    protected boolean isFocused() {
        return FlatUIUtils.isPermanentFocusOwner(list);
    }

    protected abstract StyledText getText(
        JList<? extends T> list,
        T value,
        int index,
        boolean selected,
        boolean focused
    );
}

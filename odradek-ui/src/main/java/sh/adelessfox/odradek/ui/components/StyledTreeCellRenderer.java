package sh.adelessfox.odradek.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public abstract class StyledTreeCellRenderer<T> extends StyledCellRenderer implements TreeCellRenderer {
    private JTree tree;
    private boolean selected;
    private boolean dropCell;

    private Color foregroundSelectionColor;
    private Color foregroundNonSelectionColor;
    private Color backgroundSelectionColor;
    private Color backgroundNonSelectionColor;

    public StyledTreeCellRenderer() {
        updateUI();
        setPadding(new Insets(3, 4, 3, 4));
    }

    @Override
    public void updateUI() {
        super.updateUI();

        foregroundSelectionColor = UIManager.getColor("Tree.selectionForeground");
        foregroundNonSelectionColor = UIManager.getColor("Tree.textForeground");
        backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
        backgroundNonSelectionColor = UIManager.getColor("Tree.textBackground");
    }

    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean focused
    ) {
        this.tree = tree;
        this.selected = selected;
        this.dropCell = isDropCell(tree, row);

        if (selected || dropCell) {
            setBackground(backgroundSelectionColor);
            setForeground(foregroundSelectionColor);
        } else {
            setBackground(backgroundNonSelectionColor);
            setForeground(foregroundNonSelectionColor);
        }

        var typedValue = getValue(value);

        setLeadingIcon(getIconOrDisabledIcon(tree, typedValue, selected, expanded, leaf, row, focused));
        setFont(tree.getFont());
        setText(getText(tree, typedValue, selected, expanded, focused, leaf, row));

        return this;
    }

    protected abstract T getValue(Object value);

    @Override
    protected boolean isSelected() {
        return selected || dropCell;
    }

    @Override
    protected boolean isFocused() {
        return FlatUIUtils.isPermanentFocusOwner(tree);
    }

    private Icon getIconOrDisabledIcon(
        JTree tree,
        T value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean focused
    ) {
        Icon icon = getIcon(tree, value, selected, expanded, focused, leaf, row);
        if (icon != null && !tree.isEnabled()) {
            icon = Objects.requireNonNullElse(UIManager.getLookAndFeel().getDisabledIcon(tree, icon), icon);
        }
        return icon;
    }

    protected Icon getIcon(
        JTree tree,
        T value,
        boolean selected,
        boolean expanded,
        boolean focused,
        boolean leaf,
        int row
    ) {
        if (leaf) {
            return UIManager.getIcon("Tree.leafIcon");
        } else if (expanded) {
            return UIManager.getIcon("Tree.openIcon");
        } else {
            return UIManager.getIcon("Tree.closedIcon");
        }
    }

    protected abstract StyledText getText(
        JTree tree,
        T value,
        boolean selected,
        boolean expanded,
        boolean focused,
        boolean leaf,
        int row
    );

    private static boolean isDropCell(JTree tree, int row) {
        var dropLocation = tree.getDropLocation();
        return dropLocation != null &&
            dropLocation.getChildIndex() == -1 &&
            tree.getRowForPath(dropLocation.getPath()) == row;
    }
}

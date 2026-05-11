package sh.adelessfox.odradek.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public abstract class StyledTreeCellRenderer<T> extends StyledCellRenderer implements TreeCellRenderer {
    private JTree tree;
    private boolean selected;

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

    @SuppressWarnings("unchecked")
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

        if (selected) {
            setBackground(backgroundSelectionColor);
            setForeground(foregroundSelectionColor);
        } else {
            setBackground(backgroundNonSelectionColor);
            setForeground(foregroundNonSelectionColor);
        }

        Icon icon = getIcon(tree, (T) value, selected, expanded, focused, leaf, row);
        if (icon != null && !tree.isEnabled()) {
            icon = Objects.requireNonNullElse(UIManager.getLookAndFeel().getDisabledIcon(tree, icon), icon);
        }

        setLeadingIcon(icon);
        setFont(tree.getFont());
        setText(getText(tree, (T) value, selected, expanded, focused, leaf, row));
        return this;
    }

    @Override
    protected boolean isSelected() {
        return selected;
    }

    @Override
    protected boolean isFocused() {
        return FlatUIUtils.isPermanentFocusOwner(tree);
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
}

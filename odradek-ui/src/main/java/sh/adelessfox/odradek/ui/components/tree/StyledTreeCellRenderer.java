package sh.adelessfox.odradek.ui.components.tree;

import com.formdev.flatlaf.ui.FlatUIUtils;
import sh.adelessfox.odradek.ui.components.StyledComponent;
import sh.adelessfox.odradek.ui.components.StyledFlag;
import sh.adelessfox.odradek.ui.components.StyledFragment;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.EnumSet;
import java.util.Objects;

public abstract class StyledTreeCellRenderer<T> extends StyledComponent implements TreeCellRenderer {
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

        this.foregroundSelectionColor = UIManager.getColor("Tree.selectionForeground");
        this.foregroundNonSelectionColor = UIManager.getColor("Tree.textForeground");
        this.backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
        this.backgroundNonSelectionColor = UIManager.getColor("Tree.textBackground");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean focused) {
        clear();

        this.tree = tree;
        this.selected = selected;

        Icon icon = getIcon(tree, (T) value, selected, expanded, focused, leaf, row);

        if (icon != null && !tree.isEnabled()) {
            icon = Objects.requireNonNullElse(UIManager.getLookAndFeel().getDisabledIcon(tree, icon), icon);
        }

        setBackground(selected ? backgroundSelectionColor : backgroundNonSelectionColor);
        setForeground(selected ? foregroundSelectionColor : foregroundNonSelectionColor);
        setLeadingIcon(icon);
        setFont(tree.getFont());

        customizeCellRenderer(tree, (T) value, selected, expanded, focused, leaf, row);

        return this;
    }

    @Override
    public void append(StyledFragment fragment) {
        if (selected && isFocused()) {
            var flags = EnumSet.noneOf(StyledFlag.class);
            flags.addAll(fragment.flags());
            flags.remove(StyledFlag.GRAYED);

            var newFragment = fragment
                .withForeground(getForeground())
                .withFlags(flags);

            super.append(newFragment);
        } else {
            super.append(fragment);
        }
    }

    public Icon getIcon(JTree tree, T value, boolean selected, boolean expanded, boolean focused, boolean leaf, int row) {
        if (leaf) {
            return UIManager.getIcon("Tree.leafIcon");
        } else if (expanded) {
            return UIManager.getIcon("Tree.openIcon");
        } else {
            return UIManager.getIcon("Tree.closedIcon");
        }
    }

    protected final boolean isFocused() {
        return FlatUIUtils.isPermanentFocusOwner(tree);
    }

    protected abstract void customizeCellRenderer(JTree tree, T value, boolean selected, boolean expanded, boolean focused, boolean leaf, int row);
}

package sh.adelessfox.odradek.app.component.main;

import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class ObjectTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean sel,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus
    ) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Object node = value;
        if (value instanceof TreeItem<?> item) {
            node = item.getValue();
        }
        if (node instanceof ObjectStructure element) {
            var icon = switch (element) {
                case ObjectStructure.Compound compound -> getIcon(compound.type());
                case ObjectStructure.Attr attr -> getIcon(attr.type());
                case ObjectStructure.Index index -> getIcon(index.info());
            };
            if (tree.isEnabled()) {
                setIcon(icon);
            } else {
                Icon disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(tree, icon);
                setDisabledIcon(disabledIcon != null ? disabledIcon : icon);
            }
        }

        return this;
    }

    private Icon getIcon(TypeInfo info) {
        return switch (info) {
            // case AtomTypeInfo _ -> null;
            // case ClassTypeInfo _ -> Fugue.getIcon("blue-folder");
            // case ContainerTypeInfo _ -> Fugue.getIcon("blue-folder-open");
            // case EnumTypeInfo _ -> null;
            // case PointerTypeInfo _ -> Fugue.getIcon("blue-folder-export");
            default -> Fugue.getIcon("blue-document");
        };
    }
}

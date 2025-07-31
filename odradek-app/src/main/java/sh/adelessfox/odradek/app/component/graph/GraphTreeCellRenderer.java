package sh.adelessfox.odradek.app.component.graph;

import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.ui.tree.TreeItem;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class GraphTreeCellRenderer extends DefaultTreeCellRenderer {
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
        if (node instanceof GraphStructure structure) {
            var icon = switch (structure) {
                case GraphStructure.GraphGroups _ -> Fugue.getIcon("folders-stack");
                case GraphStructure.GraphObjects _ -> Fugue.getIcon("folders-stack");
                case GraphStructure.GraphObjectSet _ -> Fugue.getIcon("folder-open-document");
                case GraphStructure.GraphObjectSetGroup _ -> Fugue.getIcon("folders");
                case GraphStructure.Group _ -> Fugue.getIcon("folders");
                case GraphStructure.GroupDependents _ -> Fugue.getIcon("folder-import");
                case GraphStructure.GroupDependencies _ -> Fugue.getIcon("folder-export");
                case GraphStructure.GroupRoots _ -> Fugue.getIcon("folder-bookmark");
                case GraphStructure.GroupObjects _ -> Fugue.getIcon("folder-open-document");
                case GraphStructure.GroupObjectSet _ -> Fugue.getIcon("folder-open-document");
                case GraphStructure.GroupObject _ -> Fugue.getIcon("blue-document");
                default -> null;
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
}

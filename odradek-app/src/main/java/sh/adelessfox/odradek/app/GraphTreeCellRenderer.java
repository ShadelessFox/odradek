package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.ui.tree.TreeItem;
import sh.adelessfox.odradek.app.ui.util.Fugue;

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
        if (node instanceof GraphStructure.Element element) {
            var icon = switch (element) {
                case GraphStructure.Element.Graph ignored -> Fugue.getIcon("folders-stack");
                case GraphStructure.Element.Group ignored -> Fugue.getIcon("folders");
                case GraphStructure.Element.GroupDependents ignored -> Fugue.getIcon("folder-import");
                case GraphStructure.Element.GroupDependencies ignored -> Fugue.getIcon("folder-export");
                case GraphStructure.Element.GroupRoots ignored -> Fugue.getIcon("folder-bookmark");
                case GraphStructure.Element.GroupObjects ignored -> Fugue.getIcon("folder-open-document");
                case GraphStructure.Element.GroupObjectSet ignored -> Fugue.getIcon("folder-open-document");
                case GraphStructure.Element.GroupObject ignored -> Fugue.getIcon("blue-document");
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

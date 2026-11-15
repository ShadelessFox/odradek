package sh.adelessfox.odradek.ui.components.tree;

import com.formdev.flatlaf.ui.FlatTreeUI;
import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.components.StyledComponent;
import sh.adelessfox.odradek.ui.util.GraphicsUtils;

import javax.swing.*;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A tree with section headings attached to ordinary nodes.
 * <p>
 * An empty title draws only a separator; an absent title leaves the row unchanged.
 */
public class TitledTree<T extends TreeStructure<T>> extends StructuredTree<T> {
    private Function<? super T, Optional<String>> titleProvider;

    public TitledTree(T structure, DataContext parentContext) {
        super(structure, parentContext);
        setLargeModel(false);
        updateUI();
    }

    public TitledTree(T structure) {
        this(structure, new DataContext());
    }

    @Override
    public void updateUI() {
        setUI(new TitledTreeUI());
        setRowHeight(0);
    }

    public Function<? super T, Optional<String>> getTitleProvider() {
        return titleProvider;
    }

    public void setTitleProvider(Function<? super T, Optional<String>> titleProvider) {
        if (this.titleProvider != titleProvider) {
            this.titleProvider = Objects.requireNonNull(titleProvider);
            revalidate();
            repaint();
        }
    }

    private final class TitledTreeUI extends FlatTreeUI {
        private Color textColor;
        private Color separatorColor;
        private int rowHeight;

        @Override
        protected void installDefaults() {
            super.installDefaults();

            textColor = UIManager.getColor("Label.disabledForeground");
            separatorColor = UIManager.getColor("Separator.foreground");
            rowHeight = UIManager.getInt("Tree.rowHeight");
        }

        @Override
        protected void uninstallDefaults() {
            super.uninstallDefaults();

            textColor = null;
            separatorColor = null;
        }

        @Override
        protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
            return new TitledNodeDimensions(super.createNodeDimensions());
        }

        @Override
        protected void paintRow(
            Graphics g,
            Rectangle clipBounds,
            Insets insets,
            Rectangle bounds,
            TreePath path,
            int row,
            boolean expanded,
            boolean hasBeenExpanded,
            boolean leaf
        ) {
            titleText(path).ifPresent(text -> paintTitle(
                g,
                bounds,
                insets,
                text,
                textOffset(path.getLastPathComponent(), row, expanded)));

            super.paintRow(
                g,
                clipBounds,
                insets,
                contentBounds(bounds, path),
                path,
                row,
                expanded,
                hasBeenExpanded,
                leaf);
        }

        private void paintTitle(Graphics g, Rectangle bounds, Insets insets, String text, int textOffset) {
            var g2 = (Graphics2D) g.create();
            try {
                GraphicsUtils.setTextRenderingHints(g2);
                g2.setFont(titleFont());

                var metrics = g2.getFontMetrics();
                int x = bounds.x + UIScale.scale(4);
                int y = bounds.y + UIScale.scale(5);

                if (!text.isEmpty()) {
                    int start = x;
                    x = bounds.x + textOffset;
                    int leadingWidth = Math.max(0, x - start - UIScale.scale(8));

                    g2.setColor(textColor);
                    g2.drawString(text, x, y + metrics.getAscent());

                    x += metrics.stringWidth(text) + UIScale.scale(8);
                    y += metrics.getHeight() / 2;

                    g2.setColor(separatorColor);
                    g2.fillRect(start, y, leadingWidth, UIScale.scale(1));
                }

                g2.setColor(separatorColor);
                g2.fillRect(x, y, Math.max(0, tree.getWidth() - insets.right - x), UIScale.scale(1));
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void paintExpandControl(
            Graphics g,
            Rectangle clipBounds,
            Insets insets,
            Rectangle bounds,
            TreePath path,
            int row,
            boolean expanded,
            boolean hasBeenExpanded,
            boolean leaf
        ) {
            super.paintExpandControl(
                g,
                clipBounds,
                insets,
                contentBounds(bounds, path),
                path,
                row,
                expanded,
                hasBeenExpanded,
                leaf);
        }

        @Override
        protected void paintHorizontalPartOfLeg(
            Graphics g,
            Rectangle clipBounds,
            Insets insets,
            Rectangle bounds,
            TreePath path,
            int row,
            boolean expanded,
            boolean hasBeenExpanded,
            boolean leaf
        ) {
            super.paintHorizontalPartOfLeg(
                g,
                clipBounds,
                insets,
                contentBounds(bounds, path),
                path,
                row,
                expanded,
                hasBeenExpanded,
                leaf);
        }

        @Override
        protected boolean isLocationInExpandControl(TreePath path, int mouseX, int mouseY) {
            var bounds = getPathBounds(tree, path);
            return bounds != null
                && mouseY >= bounds.y + titleHeight(titleText(path))
                && super.isLocationInExpandControl(path, mouseX, mouseY);
        }

        private Optional<String> titleText(TreePath path) {
            return titleProvider == null ? Optional.empty() : titleProvider.apply(getLastPathComponent(path));
        }

        private int textOffset(Object value, int row, boolean expanded) {
            var renderer = currentCellRenderer.getTreeCellRendererComponent(
                tree,
                value,
                tree.isRowSelected(row),
                expanded,
                treeModel.isLeaf(value),
                row,
                false);
            if (renderer instanceof StyledComponent styled) {
                var icon = styled.getLeadingIcon();
                return styled.getPadding().left + (icon == null ? 0 : icon.getIconWidth() + styled.getIconTextGap());
            }
            if (renderer instanceof JLabel label) {
                var icon = label.isEnabled() ? label.getIcon() : label.getDisabledIcon();
                return label.getInsets().left + (icon == null ? 0 : icon.getIconWidth() + label.getIconTextGap());
            }
            return UIScale.scale(4);
        }

        private int titleHeight(Optional<String> title) {
            return title
                .map(s -> UIScale.scale(10) + (s.isEmpty() ? 0 : getFontMetrics(titleFont()).getHeight()))
                .orElse(0);
        }

        private Font titleFont() {
            return getFont().deriveFont(Font.BOLD);
        }

        private Rectangle contentBounds(Rectangle bounds, TreePath path) {
            int height = titleHeight(titleText(path));
            return new Rectangle(bounds.x, bounds.y + height, bounds.width, bounds.height - height);
        }

        private final class TitledNodeDimensions extends AbstractLayoutCache.NodeDimensions {
            private final AbstractLayoutCache.NodeDimensions delegate;

            TitledNodeDimensions(AbstractLayoutCache.NodeDimensions delegate) {
                this.delegate = delegate;
            }

            @Override
            public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size) {
                var bounds = delegate.getNodeDimensions(value, row, depth, expanded, size);
                if (bounds != null) {
                    var title = titleText(new TreePath(value));
                    bounds.height = Math.max(bounds.height, UIScale.scale(rowHeight)) + titleHeight(title);
                    title.ifPresent(s -> bounds.width = Math.max(
                        bounds.width,
                        textOffset(value, row, expanded) + getFontMetrics(titleFont()).stringWidth(s) + UIScale.scale(8)));
                }
                return bounds;
            }
        }
    }
}

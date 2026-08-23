package sh.adelessfox.odradek.ui.components.tree;

import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.StyledTreeCellRenderer;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.GraphicsUtils;
import sh.adelessfox.odradek.ui.util.Listeners;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

public class StructuredTree<T extends TreeStructure<T>> extends JTree implements DataContext {
    private final Listeners<TreeActionListener> actionListeners = new Listeners<>(TreeActionListener.class);
    private TreeLabelProvider<T> labelProvider;
    private String placeholderText;

    // For caching last shown tooltip while hovering over the same row
    private int lastRowIndex = -1;
    private int lastRowCount = -1;

    public StructuredTree() {
        super((TreeModel) null);
        setup();
    }

    public StructuredTree(T structure) {
        super(new StructuredTreeModel<>(structure));
        setup();
    }

    private void setup() {
        addMouseListener(new MouseAdapter() {
            int lastRow = -1;

            @Override
            public void mousePressed(MouseEvent e) {
                lastRow = getRowForLocation(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)
                    && e.getClickCount() % getToggleClickCount() == 0
                    && getRowForLocation(e.getX(), e.getY()) == lastRow
                ) {
                    notifyTreeAction(e, TreeActionListener::treePathClicked);
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    notifyTreeAction(e, TreeActionListener::treePathClicked);
                }
            }
        });
        addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                notifyTreeAction(path, e, TreeActionListener::treePathSelected);
            }
        });

        setLargeModel(true);
        setExpandsSelectedPaths(true);

        // Required for TreeLabelProvider#getToolTip
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void expand() {
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
    }

    /**
     * Runs an update and restores selection by matching the old and new elements.
     *
     * @param sameElement identifies elements whose mutable properties or location may have changed
     * @param update      update that can replace or move tree nodes
     */
    public void updatePreservingSelection(BiPredicate<? super T, ? super T> sameElement, Runnable update) {
        var selection = getSelectionPaths();
        if (selection == null) {
            update.run();
            return;
        }

        var elements = Stream.of(selection)
            .map(this::getLastPathComponent)
            .toList();

        update.run();

        var paths = elements.stream()
            .map(element -> getModel().findLoadedPath(candidate -> sameElement.test(element, candidate)))
            .flatMap(Optional::stream)
            .toArray(TreePath[]::new);

        setSelectionPaths(paths);
    }

    @Override
    public Optional<?> get(String key) {
        if (DataKeys.COMPONENT.is(key)) {
            return Optional.of(this);
        }
        if (DataKeys.SELECTION.is(key)) {
            TreePath[] paths = getSelectionPaths();
            if (paths != null && paths.length == 1) {
                return Optional.of(getLastPathComponent(paths[0]));
            }
        }
        if (DataKeys.SELECTION_LIST.is(key)) {
            TreePath[] paths = getSelectionPaths();
            if (paths != null) {
                return Optional.of(Arrays.stream(paths).map(this::getLastPathComponent).toList());
            }
        }
        return Optional.empty();
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        if (event == null) {
            return null;
        }
        int offset = UIScale.scale(10);
        return new Point(event.getX() + offset, event.getY() + offset);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event == null) {
            return null;
        }

        if (labelProvider != null) {
            int rowIndex = getRowForLocation(event.getX(), event.getY());
            if (rowIndex < 0) {
                return null;
            }

            int visibleRows = getRowCount();
            if (lastRowIndex == rowIndex && lastRowCount == visibleRows) {
                return super.getToolTipText();
            }

            var path = getPathForRow(rowIndex);
            if (path == null) {
                return null;
            }

            var element = unwrap(path.getLastPathComponent());
            if (element == null) {
                return null;
            }

            lastRowIndex = rowIndex;
            lastRowCount = visibleRows;
            putClientProperty(TOOL_TIP_TEXT_KEY, labelProvider.getToolTip(element).orElse(null));

            return super.getToolTipText();
        }

        return super.getToolTipText(event);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (placeholderText != null && (getModel() == null || getModel().isEmpty())) {
            GraphicsUtils.setTextRenderingHints(g);
            GraphicsUtils.drawCenteredString(g, placeholderText, this);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public StructuredTreeModel<T> getModel() {
        return (StructuredTreeModel<T>) super.getModel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setModel(TreeModel newModel) {
        setModel((StructuredTreeModel<T>) newModel);
    }

    public void setModel(StructuredTreeModel<T> newModel) {
        super.setModel(newModel);
    }

    public TreeLabelProvider<T> getLabelProvider() {
        return labelProvider;
    }

    public void setLabelProvider(TreeLabelProvider<T> labelProvider) {
        if (this.labelProvider != labelProvider) {
            this.labelProvider = labelProvider;

            if (labelProvider != null) {
                setCellRenderer(new LabelProviderTreeCellRenderer<>(labelProvider, this::unwrap));
            } else {
                setCellRenderer(null);
            }
        }
    }

    public void addActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.add(listener);
    }

    public void removeActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.remove(listener);
    }

    public String getPlaceholderText() {
        return placeholderText;
    }

    public void setPlaceholderText(String placeholderText) {
        if (!Objects.equals(this.placeholderText, placeholderText)) {
            this.placeholderText = placeholderText;
            repaint();
        }
    }

    private void notifyTreeAction(EventObject event, BiConsumer<TreeActionListener, TreeActionEvent> consumer) {
        var paths = getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            notifyTreeAction(path, event, consumer);
        }
    }

    private void notifyTreeAction(
        TreePath path,
        EventObject event,
        BiConsumer<TreeActionListener, TreeActionEvent> consumer
    ) {
        consumer.accept(actionListeners.broadcast(), new TreeActionEvent(event, path, getRowForPath(path)));
    }

    public T getSelectionPathComponent() {
        return getLastPathComponent(getSelectionPath());
    }

    public T getLastPathComponent(TreePath path) {
        if (path == null) {
            return null;
        }
        return unwrap(path.getLastPathComponent());
    }

    @SuppressWarnings("unchecked")
    private T unwrap(Object value) {
        if (value instanceof TreeItem<?> item) {
            value = item.getValue();
        }
        return (T) value;
    }

    private static class LabelProviderTreeCellRenderer<T> extends StyledTreeCellRenderer<T> {
        private final TreeLabelProvider<T> labelProvider;
        private final Function<Object, T> mapper;

        public LabelProviderTreeCellRenderer(TreeLabelProvider<T> labelProvider, Function<Object, T> mapper) {
            this.labelProvider = labelProvider;
            this.mapper = mapper;
        }

        @Override
        protected T getValue(Object value) {
            return mapper.apply(value);
        }

        @Override
        protected StyledText getText(
            JTree tree,
            T value,
            boolean selected,
            boolean expanded,
            boolean focused,
            boolean leaf,
            int row
        ) {
            var text = switch (labelProvider) {
                case StyledTreeLabelProvider<T> p -> p.getStyledText(value);
                case TreeLabelProvider<T> p -> p.getText(value).map(StyledText::of);
            };
            return text.orElse(StyledText.of());
        }

        @Override
        protected Icon getIcon(
            JTree tree,
            T value,
            boolean selected,
            boolean expanded,
            boolean focused,
            boolean leaf,
            int row
        ) {
            return labelProvider.getIcon(value).orElse(null);
        }
    }
}

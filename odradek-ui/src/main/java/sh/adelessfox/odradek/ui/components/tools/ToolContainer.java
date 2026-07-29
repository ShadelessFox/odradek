package sh.adelessfox.odradek.ui.components.tools;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.components.LineBorder;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

public final class ToolContainer extends JComponent implements ToolManager {
    private final Map<String, ToolPanelState> panelById = new HashMap<>();
    private final Map<ToolPanel.Placement, List<ToolPanelState>> groupByPlacement = new HashMap<>();

    private final SplitterContainer container;
    private final ToolButtonPanel leftButtons;
    private final ToolButtonPanel rightButtons;

    public ToolContainer() {
        container = new SplitterContainer();
        leftButtons = new ToolButtonPanel();
        rightButtons = new ToolButtonPanel();

        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);

        updateButtons();
        ToolButtonRepainter.install();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setCenter(JComponent component) {
        container.setCenter(component);
    }

    @Override
    public void addPanel(ToolPanel.Provider provider, ToolPanel.Placement placement) {
        var state = new ToolPanelState(provider, placement);
        if (panelById.putIfAbsent(provider.id(), state) != null) {
            throw new IllegalArgumentException("Tool panel with ID '" + provider.id() + "' is already registered");
        }
        getGroupFor(placement).add(state);
        updateButtons();
    }

    @Override
    public void openPanel(String id, boolean focus) {
        var state = findPanel(id);
        if (state.open) {
            return;
        }

        var group = getGroupFor(state.placement);
        for (ToolPanelState other : group) {
            closePanel(other.provider.id());
        }

        if (state.panel == null) {
            state.panel = state.provider.create(state);
        }

        openPanel(state, focus);

        leftButtons.repaint();
        rightButtons.repaint();
    }

    @Override
    public void closePanel(String id) {
        var state = findPanel(id);
        if (!state.open) {
            return;
        }

        container.setComponent(null, state.placement);
        state.open = false;

        leftButtons.repaint();
        rightButtons.repaint();
    }

    @Override
    public void movePanel(String id, ToolPanel.Placement placement, int index) {
        var state = findPanel(id);
        if (state.placement.equals(placement)) {
            return;
        }

        if (state.open) {
            var group = getGroupFor(placement);
            for (ToolPanelState other : group) {
                if (other != state) {
                    closePanel(other.provider.id());
                }
            }

            container.setComponent(null, state.placement);
        }

        var moved = state.movedTo(placement);
        panelById.put(id, moved);
        getGroupFor(state.placement).remove(state);
        getGroupFor(placement).add(index, moved);

        if (moved.open) {
            openPanel(moved, false);
        }

        updateButtons();
    }

    @Override
    public ToolState getState() {
        return new ToolState(
            getState(ToolPanel.Placement.Anchor.LEFT),
            getState(ToolPanel.Placement.Anchor.RIGHT),
            getState(ToolPanel.Placement.Anchor.BOTTOM)
        );
    }

    @Override
    public void setState(ToolState state) {
        setState(ToolPanel.Placement.Anchor.LEFT, state.left());
        setState(ToolPanel.Placement.Anchor.RIGHT, state.right());
        setState(ToolPanel.Placement.Anchor.BOTTOM, state.bottom());
    }

    private ToolState.Anchor getState(ToolPanel.Placement.Anchor anchor) {
        var primary = getState(new ToolPanel.Placement(anchor, true));
        var secondary = getState(new ToolPanel.Placement(anchor, false));
        var weights = container.getWeights(anchor);
        return new ToolState.Anchor(primary, secondary, weights);
    }

    private ToolState.Anchor.Group getState(ToolPanel.Placement placement) {
        var ids = new ArrayList<String>();
        var selection = OptionalInt.empty();

        var group = getGroupFor(placement);
        for (ToolPanelState pane : group) {
            if (pane.open) {
                selection = OptionalInt.of(ids.size());
            }
            ids.add(pane.provider.id());
        }

        return new ToolState.Anchor.Group(ids, selection);
    }

    private void setState(ToolPanel.Placement.Anchor anchor, ToolState.Anchor state) {
        setState(new ToolPanel.Placement(anchor, true), state.primary());
        setState(new ToolPanel.Placement(anchor, false), state.secondary());
        container.setWeights(anchor, state.weights());
    }

    private void setState(ToolPanel.Placement placement, ToolState.Anchor.Group state) {
        for (int i = 0; i < state.tools().size(); i++) {
            movePanel(state.tools().get(i), placement, i);
        }
        for (ToolPanelState pane : getGroupFor(placement)) {
            var selection = state.selection();
            if (selection.isPresent() && pane.provider.id().equals(state.tools().get(selection.getAsInt()))) {
                openPanel(pane.provider.id(), false);
            } else {
                closePanel(pane.provider.id());
            }
        }
    }

    private void togglePanel(ToolPanel.Provider provider) {
        togglePanel(provider.id());
    }

    private void togglePanel(String id) {
        ToolPanelState state = findPanel(id);
        if (state.open) {
            closePanel(id);
        } else {
            openPanel(id, true);
        }
    }

    private void openPanel(ToolPanelState state, boolean focus) {
        if (state.component == null) {
            var header = new ToolPanelHeader();
            header.setLabel(state.provider.name());

            var holder = new JPanel(new BorderLayout());
            holder.putClientProperty(FlatClientProperties.STYLE_CLASS, "ToolPanel.content");
            holder.add(header, BorderLayout.NORTH);
            holder.add(state.panel.createComponent());

            state.component = holder;
            state.header = header;

            if (state.trailingComponent != null) {
                header.setTrailingComponent(state.trailingComponent);
                state.trailingComponent = null;
            }
        }

        container.setComponent(state.component, state.placement);
        state.open = true;

        if (focus) {
            state.component.requestFocusInWindow();
        }
    }


    private ToolPanelState findPanel(String id) {
        var state = panelById.get(id);
        if (state == null) {
            throw new IllegalArgumentException("No tool panel with ID '" + id + "' is registered");
        }
        return state;
    }

    private void updateButtons() {
        var leftPrimary = getGroupFor(ToolPanel.Placement.Anchor.LEFT, true);
        var leftSecondary = getGroupFor(ToolPanel.Placement.Anchor.LEFT, false);
        var leftBottom = getGroupFor(ToolPanel.Placement.Anchor.BOTTOM, true);
        rebuildButtons(leftButtons, leftPrimary, leftSecondary, leftBottom);

        var rightPrimary = getGroupFor(ToolPanel.Placement.Anchor.RIGHT, true);
        var rightSecondary = getGroupFor(ToolPanel.Placement.Anchor.RIGHT, false);
        var rightBottom = getGroupFor(ToolPanel.Placement.Anchor.BOTTOM, false);
        rebuildButtons(rightButtons, rightPrimary, rightSecondary, rightBottom);
    }

    private List<ToolPanelState> getGroupFor(ToolPanel.Placement.Anchor anchor, boolean primary) {
        return getGroupFor(new ToolPanel.Placement(anchor, primary));
    }

    private List<ToolPanelState> getGroupFor(ToolPanel.Placement placement) {
        return groupByPlacement.computeIfAbsent(placement, _ -> new ArrayList<>());
    }

    private void rebuildButtons(
        ToolButtonPanel strip,
        List<ToolPanelState> primary,
        List<ToolPanelState> secondary,
        List<ToolPanelState> bottom
    ) {
        strip.removeAll();
        for (ToolPanelState state : primary) {
            strip.add(createButton(state));
        }
        if (!primary.isEmpty() && !secondary.isEmpty()) {
            strip.add(new JSeparator(), "al center,w 16");
        }
        for (ToolPanelState state : secondary) {
            strip.add(createButton(state));
        }
        if (!bottom.isEmpty()) {
            strip.add(Box.createVerticalGlue(), "pushy,growy");
        }
        for (ToolPanelState state : bottom) {
            strip.add(createButton(state));
        }
        strip.setVisible(strip.getComponentCount() > 0);
        strip.revalidate();
        strip.repaint();
    }

    private ToolButton createButton(ToolPanelState state) {
        var button = new ToolButton(
            state.provider,
            this::isPanelSelected,
            this::isPanelFocused,
            this::togglePanel);
        button.setComponentPopupMenu(createButtonPopupMenu(state));
        return button;
    }

    private JPopupMenu createButtonPopupMenu(ToolPanelState state) {
        var moveTo = new JMenu("Move to");

        var placements = List.of(
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.LEFT, true),
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.LEFT, false),
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.BOTTOM, true),
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.BOTTOM, false),
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.RIGHT, false),
            new ToolPanel.Placement(ToolPanel.Placement.Anchor.RIGHT, true));

        for (ToolPanel.Placement placement : placements) {
            var action = new AbstractAction(getLabelForPlacement(placement)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    movePanel(state.provider.id(), placement, 0);
                }
            };
            action.putValue(Action.SMALL_ICON, getIconForPlacement(placement));
            action.setEnabled(!placement.equals(state.placement));
            moveTo.add(action);
        }

        var menu = new JPopupMenu();
        menu.add(moveTo);

        return menu;
    }

    private static String getLabelForPlacement(ToolPanel.Placement placement) {
        return switch (placement.anchor()) {
            case LEFT -> placement.primary() ? "Left Top" : "Left Bottom";
            case RIGHT -> placement.primary() ? "Right Top" : "Right Bottom";
            case BOTTOM -> placement.primary() ? "Bottom Left" : "Bottom Right";
        };
    }

    private static Icon getIconForPlacement(ToolPanel.Placement placement) {
        return switch (placement.anchor()) {
            case LEFT -> Fugue.getIcon("application-dock-180");
            case RIGHT -> Fugue.getIcon("application-dock");
            case BOTTOM -> Fugue.getIcon("application-dock-270");
        };
    }

    private boolean isPanelSelected(ToolPanel.Provider provider) {
        return findPanel(provider.id()).open;
    }

    private boolean isPanelFocused(ToolPanel.Provider provider) {
        var component = findPanel(provider.id()).component;
        if (component == null) {
            return false;
        }
        var focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        var focusOwner = focusManager.getPermanentFocusOwner();
        return focusOwner != null
            && SwingUtilities.isDescendingFrom(focusOwner, component)
            && isInActiveWindow(focusOwner, focusManager.getActiveWindow());
    }

    // com.formdev.flatlaf.ui.FlatUIUtils.isInActiveWindow
    static boolean isInActiveWindow(Component c, Window activeWindow) {
        var window = SwingUtilities.windowForComponent(c);
        return window == activeWindow
            || window != null && window.getType() == Window.Type.POPUP && window.getOwner() == activeWindow;
    }

    private static final class ToolPanelState implements ToolSite {
        private final ToolPanel.Provider provider;
        private final ToolPanel.Placement placement;
        private ToolPanel panel;
        private JComponent component;
        private ToolPanelHeader header;
        private boolean open;

        private JComponent trailingComponent;

        ToolPanelState(ToolPanel.Provider provider, ToolPanel.Placement placement) {
            this.provider = provider;
            this.placement = placement;
        }

        @Override
        public void setTrailingComponent(JComponent component) {
            if (header == null) {
                trailingComponent = component;
            } else {
                header.setTrailingComponent(component);
            }
        }

        private ToolPanelState movedTo(ToolPanel.Placement placement) {
            var moved = new ToolPanelState(provider, placement);
            moved.panel = panel;
            moved.component = component;
            moved.header = header;
            moved.open = open;
            return moved;
        }
    }

    private static final class ToolButtonPanel extends JPanel {
        ToolButtonPanel() {
            setLayout(new MigLayout("ins 0 4 0 4,gap 4,flowy"));
        }
    }

    private static final class ToolButtonRepainter implements PropertyChangeListener {
        private static ToolButtonRepainter instance;
        private final KeyboardFocusManager keyboardFocusManager;

        static void install() {
            synchronized (ToolButtonRepainter.class) {
                if (instance != null) {
                    return;
                }
                instance = new ToolButtonRepainter();
            }
        }

        ToolButtonRepainter() {
            keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            keyboardFocusManager.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "permanentFocusOwner" -> {
                    Object oldValue = e.getOldValue();
                    Object newValue = e.getNewValue();
                    if (oldValue instanceof Component component) {
                        repaintSelectedPaneButtons(component);
                    }
                    if (newValue instanceof Component component) {
                        repaintSelectedPaneButtons(component);
                    }
                }
                case "activeWindow" -> {
                    Component permanentFocusOwner = keyboardFocusManager.getPermanentFocusOwner();
                    if (permanentFocusOwner != null) {
                        repaintSelectedPaneButtons(permanentFocusOwner);
                    }
                }
            }
        }

        private static void repaintSelectedPaneButtons(Component c) {
            if (c instanceof ToolContainer panel) {
                repaintSelectedPaneButton(panel);
            }
            for (Component c2 = c; (c2 = SwingUtilities.getAncestorOfClass(ToolContainer.class, c2)) != null; ) {
                repaintSelectedPaneButton((ToolContainer) c2);
            }
        }
    }

    private static void repaintSelectedPaneButton(ToolContainer manager) {
        manager.leftButtons.repaint();
        manager.rightButtons.repaint();
    }

    private static class ToolPanelHeader extends JPanel {
        private int height;
        private Color backgroundStart;
        private Color backgroundEnd;

        private final JLabel label;
        private JComponent trailingComponent;

        public ToolPanelHeader() {
            label = new JLabel();

            setBorder(LineBorder.of(0, 0, 1, 0));
            setLayout(new MigLayout("ins 0", "", "[grow,fill]"));
            add(label, "gap 6");
            add(Box.createHorizontalGlue(), "push,grow");

            updateUI();
        }

        void setLabel(String label) {
            this.label.setText(label);
        }

        void setTrailingComponent(JComponent trailingComponent) {
            if (this.trailingComponent != null) {
                remove(trailingComponent);
            }
            this.trailingComponent = trailingComponent;
            if (trailingComponent != null) {
                add(trailingComponent);
            }
        }

        @Override
        public void updateUI() {
            super.updateUI();

            height = UIManager.getInt("ToolPanelHeader.height");
            backgroundStart = UIManager.getColor("ToolPanelHeader.backgroundStart");
            backgroundEnd = UIManager.getColor("ToolPanelHeader.backgroundEnd");
        }

        @Override
        protected void paintComponent(Graphics g) {
            var g2 = (Graphics2D) g.create();
            try {
                g2.setPaint(new GradientPaint(0, 0, backgroundStart, 0, getHeight(), backgroundEnd));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g2.dispose();
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, height);
        }
    }
}

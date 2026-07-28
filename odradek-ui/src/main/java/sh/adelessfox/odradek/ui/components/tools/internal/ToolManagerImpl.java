package sh.adelessfox.odradek.ui.components.tools.internal;

import sh.adelessfox.odradek.ui.components.tools.ToolManager;
import sh.adelessfox.odradek.ui.components.tools.ToolPanel;
import sh.adelessfox.odradek.ui.components.tools.ToolState;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

public final class ToolManagerImpl implements ToolManager {
    private final Map<String, ToolPanelState> panelById = new HashMap<>();
    private final Map<ToolPanel.Placement, List<ToolPanelState>> groupByPlacement = new HashMap<>();

    private final SplitterContainer container;
    private final ToolButtonPanel leftButtons;
    private final ToolButtonPanel rightButtons;
    private final JPanel root;

    public ToolManagerImpl() {
        container = new SplitterContainer();
        leftButtons = new ToolButtonPanel(true);
        rightButtons = new ToolButtonPanel(false);

        root = new JPanel();
        root.setLayout(new BorderLayout());
        root.add(container, BorderLayout.CENTER);
        root.add(leftButtons, BorderLayout.WEST);
        root.add(rightButtons, BorderLayout.EAST);
        root.putClientProperty(ToolManagerImpl.class, this); // required for ButtonRepainter

        updateButtons();
        ButtonRepainter.install();
    }

    @Override
    public JComponent getComponent() {
        return root;
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
            state.panel = state.provider.create();
        }

        if (state.component == null) {
            state.component = state.panel.createComponent();
        }

        container.setComponent(state.component, state.placement);
        state.open = true;

        if (focus) {
            state.component.requestFocusInWindow();
        }

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
            if (moved.component == null) {
                moved.component = moved.panel.createComponent();
            }
            container.setComponent(moved.component, placement);
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
            strip.add(new JSeparator());
        }
        for (ToolPanelState state : secondary) {
            strip.add(createButton(state));
        }
        if (!bottom.isEmpty()) {
            strip.add(Box.createVerticalGlue());
        }
        for (ToolPanelState state : bottom) {
            strip.add(createButton(state));
        }
        strip.setVisible(strip.getComponentCount() > 0);
        strip.revalidate();
        strip.repaint();
    }

    private ToolButton createButton(ToolPanelState state) {
        return new ToolButton(state.provider, this::isPanelSelected, this::isPanelFocused, this::togglePanel);
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

    private static final class ToolPanelState {
        private final ToolPanel.Provider provider;
        private final ToolPanel.Placement placement;
        private ToolPanel panel;
        private JComponent component;
        private boolean open;

        ToolPanelState(ToolPanel.Provider provider, ToolPanel.Placement placement) {
            this.provider = provider;
            this.placement = placement;
        }

        private ToolPanelState movedTo(ToolPanel.Placement placement) {
            var moved = new ToolPanelState(provider, placement);
            moved.panel = panel;
            moved.component = component;
            moved.open = open;
            return moved;
        }
    }

    private static final class ToolButtonPanel extends JPanel {
        ToolButtonPanel(boolean left) {
            setBorder(BorderFactory.createMatteBorder(
                0,
                left ? 0 : 1,
                0,
                left ? 1 : 0,
                UIManager.getColor("Component.borderColor")));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }
    }

    private static class Host extends JComponent {
    }

    private static class ButtonRepainter implements PropertyChangeListener {
        private static ButtonRepainter instance;
        private final KeyboardFocusManager keyboardFocusManager;

        static void install() {
            synchronized (ButtonRepainter.class) {
                if (instance != null) {
                    return;
                }
                instance = new ButtonRepainter();
            }
        }

        ButtonRepainter() {
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
            while (c != null) {
                if (c instanceof JComponent jc) {
                    var manager = (ToolManagerImpl) jc.getClientProperty(ToolManagerImpl.class);
                    if (manager != null) {
                        manager.leftButtons.repaint();
                        manager.rightButtons.repaint();
                    }
                }
                c = c.getParent();
            }
        }
    }
}

package sh.adelessfox.odradek.ui.components.tool;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel with buttons on either sides that reveal contents when clicked.
 * Clicking on an already selected button will unselect it and hide the contents.
 */
public final class ToolPanelContainer extends JComponent {
    private final Splitter groupSplitter = new Splitter(true); // splitter between primary and secondary groups
    private final Splitter outerSplitter = new Splitter(false); // splitter between the panel and contents
    private final JPanel buttonsPanel;

    private final List<ToolPanelButton> primaryButtons = new ArrayList<>();
    private final List<ToolPanelButton> secondaryButtons = new ArrayList<>();
    private final ToolPanelGroup primaryGroup = new ToolPanelGroup();
    private final ToolPanelGroup secondaryGroup = new ToolPanelGroup();
    private final Placement placement;

    public enum Placement {
        LEFT,
        RIGHT
    }

    public ToolPanelContainer(Placement placement) {
        this.placement = placement;
        this.buttonsPanel = createButtonPane(placement);

        setLayout(new BorderLayout());
        add(outerSplitter, BorderLayout.CENTER);
        add(buttonsPanel, switch (placement) {
            case LEFT -> BorderLayout.WEST;
            case RIGHT -> BorderLayout.EAST;
        });

        ButtonRepainter.install();
    }

    public JComponent getContent() {
        return switch (placement) {
            case LEFT -> outerSplitter.getSecondComponent();
            case RIGHT -> outerSplitter.getFirstComponent();
        };
    }

    public void setContent(JComponent content) {
        switch (placement) {
            case LEFT -> outerSplitter.setSecondComponent(content);
            case RIGHT -> outerSplitter.setFirstComponent(content);
        }
    }

    public void addPrimaryPanel(String text, Icon icon, ToolPanel panel) {
        addPanel(text, icon, panel, true);
    }

    public void addSecondaryPanel(String text, Icon icon, ToolPanel panel) {
        addPanel(text, icon, panel, false);
    }

    private void addPanel(String text, Icon icon, ToolPanel panel, boolean primary) {
        var panelGroup = primary ? primaryGroup : secondaryGroup;
        panelGroup.addPanel(panel);

        var callback = (Runnable) () -> selectPanel(panelGroup, panel, !panelGroup.isSelected(panel));
        var button = new ToolPanelButton(panelGroup, panel, icon, callback);
        button.setToolTipText(text);

        var buttonGroup = primary ? primaryButtons : secondaryButtons;
        buttonGroup.add(button);

        // TODO: Find a better way to insert a separator when both groups are present
        buttonsPanel.removeAll();
        primaryButtons.forEach(buttonsPanel::add);
        int separatorIndex = buttonsPanel.getComponentCount();
        secondaryButtons.forEach(buttonsPanel::add);
        if (buttonsPanel.getComponentCount() != separatorIndex) {
            buttonsPanel.add(new JSeparator(), "growx", separatorIndex);
        }
    }

    public void showPanel(ToolPanel panel) {
        selectPanel(panel, true);
    }

    public void hidePanel(ToolPanel panel) {
        selectPanel(panel, false);
    }

    private void selectPanel(ToolPanel panel, boolean select) {
        if (primaryGroup.hasPanel(panel)) {
            selectPanel(primaryGroup, panel, select);
        } else {
            selectPanel(secondaryGroup, panel, select);
        }
    }

    private void selectPanel(ToolPanelGroup group, ToolPanel panel, boolean select) {
        if (!group.selectPanel(select ? panel : null)) {
            return;
        }

        boolean hasPrimary = primaryGroup.hasSelection();
        boolean hasSecondary = secondaryGroup.hasSelection();

        groupSplitter.setFirstComponent(hasPrimary ? primaryGroup.getComponent() : null);
        groupSplitter.setSecondComponent(hasSecondary ? secondaryGroup.getComponent() : null);

        var content = hasPrimary || hasSecondary ? groupSplitter : null;
        switch (placement) {
            case LEFT -> outerSplitter.setFirstComponent(content);
            case RIGHT -> outerSplitter.setSecondComponent(content);
        }

        if (select) {
            panel.setFocus();
        } else if (getContent() instanceof Focusable focusable) {
            focusable.setFocus();
        }
    }

    private static JPanel createButtonPane(Placement placement) {
        var border = switch (placement) {
            case LEFT -> BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor"));
            case RIGHT -> BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Component.borderColor"));
        };

        var panel = new JPanel();
        panel.setLayout(new MigLayout("ins 0,gap 0,wrap"));
        panel.setBorder(border);

        return panel;
    }

    private static final class Splitter extends JComponent {
        private final JSplitPane pane;
        private JComponent firstComponent;
        private JComponent secondComponent;

        Splitter(boolean vertical) {
            pane = new JSplitPane(vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
            pane.setLeftComponent(null);
            pane.setRightComponent(null);
            pane.setContinuousLayout(true);

            setLayout(new BorderLayout());
        }

        JComponent getFirstComponent() {
            return firstComponent;
        }

        void setFirstComponent(JComponent component) {
            if (this.firstComponent != component) {
                expand(component, true);
                this.firstComponent = component;
            }
        }

        JComponent getSecondComponent() {
            return secondComponent;
        }

        void setSecondComponent(JComponent component) {
            if (this.secondComponent != component) {
                expand(component, false);
                this.secondComponent = component;
            }
        }

        private void expand(Component component, boolean left) {
            var opposite = left ? secondComponent : firstComponent;
            var dividerLocation = computeDividerLocation();

            removeAll();

            if (component == null) {
                if (opposite != null) {
                    add(opposite, BorderLayout.CENTER);
                }
            } else if (opposite == null) {
                add(component, BorderLayout.CENTER);
            } else {
                add(pane, BorderLayout.CENTER);
                pane.setLeftComponent(left ? component : opposite);
                pane.setRightComponent(left ? opposite : component);
                pane.setDividerLocation(dividerLocation);
            }

            revalidate();
        }

        private int computeDividerLocation() {
            var location = pane.getDividerLocation();
            if (location != -1) {
                return location;
            }

            Dimension size = getSize();
            if (size.width == 0 && size.height == 0) {
                return -1;
            }

            if (pane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                return size.height / 2;
            } else {
                return size.width / 2;
            }
        }
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
            if (c instanceof ToolPanelContainer panel) {
                repaintSelectedPaneButton(panel);
            }
            for (Component c2 = c; (c2 = SwingUtilities.getAncestorOfClass(ToolPanelContainer.class, c2)) != null; ) {
                repaintSelectedPaneButton((ToolPanelContainer) c2);
            }
        }

        private static void repaintSelectedPaneButton(ToolPanelContainer panel) {
            panel.buttonsPanel.repaint();
        }
    }
}
